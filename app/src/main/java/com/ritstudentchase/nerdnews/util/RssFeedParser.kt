package com.ritstudentchase.nerdnews.util

import android.util.Log
import android.util.Xml
import androidx.compose.runtime.MutableState
import com.ritstudentchase.nerdnews.viewmodels.CANNOT_FETCH_CONTENT_RIGHT_NOW_ERROR
import com.ritstudentchase.nerdnews.viewmodels.RequestCode
import com.ritstudentchase.nerdnews.viewmodels.RequestResult
import com.ritstudentchase.nerdnews.viewmodels.UPDATED
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Provides scaffolding with default implementations for parsing RSS feeds.
 */
interface RssFeedParser<T_CHANNEL, T_ITEM> {
    /**
     * Override to add channel creation logic.
     */
    fun readChannel(parser: XmlPullParser, itemName: String = "item"): Pair<T_CHANNEL, List<T_ITEM>>?

    /**
     * Override to add item creation logic.
     */
    fun readItem(parser: XmlPullParser): T_ITEM

    fun getRemoteFeedItems(
        channelName: String = "channel",
        itemName: String = "item",
        channelBusinessLogic: (T_CHANNEL, List<T_ITEM>) -> Unit
    ): RequestResult
    {
        Log.d("mergeRemoteFeedItems", "Fetching upstream information from Chocolately.")
        // Check network state
        //val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //val info = cm.activeNetwork // Requires android 23

        val url = URL("https://feeds.feedburner.com/ChocolateyBlog")
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        // TODO: check internet status
        try {
            val stream = BufferedInputStream(urlConnection.getInputStream())
            val (incomingChannel, incomingItems) = parse(stream, channelName, itemName) // return result if either is null
                ?: return RequestResult(
                    false,
                    CANNOT_FETCH_CONTENT_RIGHT_NOW_ERROR,
                    RequestCode.BadParse
                )
            channelBusinessLogic(incomingChannel, incomingItems)

            return RequestResult(
                true,
                UPDATED,
                RequestCode.UpToDate
            )
        } finally {
            urlConnection.disconnect()
        }
    }

    /**
     * Reads the text from a specific node.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    fun readText(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return result
    }

    /**
     * Parses an input stream containing RSS feed content.
     */
    private fun parse(inputStream: InputStream, channelName: String, itemName: String): Pair<T_CHANNEL, List<T_ITEM>>? {
        Log.d("RSS Feed", "Parsing RSS Feed")
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            // Do not process namespaces
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag() // start
            return readFeed(parser, channelName, itemName)
        }
    }

    /**
     * Used internally to begin reading the channel.
     */
    private fun readFeed(parser: XmlPullParser, channelName: String, itemName: String): Pair<T_CHANNEL, List<T_ITEM>>? {
        Log.d("RSS Feed", "Reading RSS Feed")
        parser.require(XmlPullParser.START_TAG, null, "rss")
        // While the parser has something to parse
        while(parser.next() != XmlPullParser.END_TAG) {
            // Iterate until we find our starting position
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == channelName) {
                return readChannel(parser, itemName)
            }
        }
        return null
    }
}