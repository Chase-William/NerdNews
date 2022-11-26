package com.ritstudentchase.nerdnews.viewmodels

/**
 * https://developer.android.com/training/basics/network-ops/xml -- Used to learn
 */

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.util.Xml
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ritstudentchase.nerdnews.R
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
// import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class RequestResult(
    val success: Boolean,
    val message: String,
    val code: RequestCode
)

enum class RequestCode {
    NoInternet, // No internet for operation
    BadParse, // parse failed, was the content empty?
    UpToDate
}

class ChocolateyFeedViewModel(
    /**
     * Requires database access to manage remote (feed-items / channel) integration with local store.
     */
    private val database: NerdNewsDatabase,
    private val context: Context
) {
    private var itemsDao = database.chocolateyItemDao()
    private var channelDao = database.chocolateyChannelDao()

    // channel and items, load with initial database information
    private val channel: MutableState<ChocolateyChannel?> = mutableStateOf(null)
    private val items: MutableList<ChocolateyItem?> = mutableStateListOf()

    fun getChannel() = channel
    fun getItems() = items

    suspend fun loadLocal() {
        withContext(Dispatchers.IO) {
            channel.value = channelDao.getAll().firstOrNull()
            itemsDao.getAll().collect() {
                items.addAll(it)
            }
        }
    }

    /**
     * Request new items from Chocolatey's servers and merges them into the list of feed items if needed.
     */
    fun mergeRemoteFeedItems(): RequestResult {
        Log.d("mergeRemoteFeedItems", "Fetching upstream information from Chocolately.")
        // Check network state
        //val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //val info = cm.activeNetwork // Requires android 23



        val url = URL("https://feeds.feedburner.com/ChocolateyBlog")
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        // TODO: check internet status
        try {
            val stream = BufferedInputStream(urlConnection.getInputStream())
            val (newChannel, newItems) = parse(stream) // return result if either is null
                ?: return RequestResult(
                    false,
                    context.getString(R.string.cannot_fetch_content_right_now),
                    RequestCode.BadParse
                )

            // Begin update logic if the current channel is not null
            if (channel.value != null) {
                val currentChannel = channel.value!!
                // If pubDate is equal, no new content to report
                if (currentChannel.pubDate != newChannel!!.pubDate) {
                    // 1. Update channel
                    channelDao.update(newChannel)
                    channel.value = newChannel
                    // 2. Determine which items are new and handle them
                    for (item in newItems) {
                        // If new, insert
                        if (items.all { it!!.guid != item.guid }) {
                            items.add(item)
                            itemsDao.insertAll(item)
                        }
                    }
                }
            }
            // Begin insertion logic
            else {
                channel.value = newChannel
                channelDao.insert(newChannel!!)
                itemsDao.insertAll(newItems)
                items.addAll(newItems)
            }

            return RequestResult(
                true,
                context.getString(R.string.updated),
                RequestCode.UpToDate
            )
        } finally {
            urlConnection.disconnect()
        }
    }

    /**
     * Parses an input stream containing Chocolately RSS feed content.
     */
    private fun parse(inputStream: InputStream): Pair<ChocolateyChannel?, List<ChocolateyItem>>? {
        Log.d("Chocolatey", "Parsing RSS Feed")
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            // Do not process namespaces
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag() // start
            return readFeed(parser)
        }
    }

    /**
     * Handles the parsing of the feed.
     */
    private fun readFeed(parser: XmlPullParser): Pair<ChocolateyChannel?, List<ChocolateyItem>>? {
        Log.d("Chocolatey", "Reading RSS Feed")
        parser.require(XmlPullParser.START_TAG, null, "rss")
        // While the parser has something to parse
        while(parser.next() != XmlPullParser.END_TAG) {
            // Iterate until we find our starting position
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "channel") {
                return readChannel(parser)
            }
        }
        return null
    }

    /**
     * Handles the parsing of a channel.
     */
    private fun readChannel(parser: XmlPullParser): Pair<ChocolateyChannel?, List<ChocolateyItem>> {
        Log.d("Chocolatey", "Reading RSS Channel")
        parser.require(XmlPullParser.START_TAG, null, "channel")
        val items = mutableListOf<ChocolateyItem>()
        // parse channel
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var copyright: String? = null
        var managingEditor: String? = null
        var pubDate: String? = null
        var lastBuildDate: String? = null

        // parse channel items
        while (parser.next() != XmlPullParser.END_TAG) {
            // Iterate until we find our starting position
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            /** Fields to be accounted for:
            val title: String,
            val link: String,
            val description: String,
            val copyright: String,
            val managingEditor: String,
            val pubDate: String,
            val lastBuildDate: String,
            val items: Array<ChocolateyItem>
             */
            when (parser.name) {
                "title" -> title = readText(parser, "title")
                "link" -> link = readText(parser, "link")
                "description" -> description = readText(parser, "description")
                "copyright" -> copyright = readText(parser, "copyright")
                "managingEditor" -> managingEditor = readText(parser, "managingEditor")
                "pubDate" -> pubDate = readText(parser, "pubDate")
                "lastBuildDate" -> lastBuildDate = readText(parser, "lastBuildDate")
            }
            // Read items of the channel, items themselves are structures
            if (parser.name == "item") {
                items.add(readItem(parser))
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "channel")
        return Pair(
            ChocolateyChannel(
                pubDate!!,
                title!!,
                link!!,
                description!!,
                copyright!!,
                managingEditor!!,
                lastBuildDate!!),
            items
        )
    }

    /**
     * Handles the parsing of an item.
     */
    private fun readItem(parser: XmlPullParser): ChocolateyItem {
        Log.d("Chocolatey", "Reading RSS Item")
        parser.require(XmlPullParser.START_TAG, null, "item")
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var author: String? = null
        var guid: String? = null
        var pubDate: String? = null
        var content: String? = null
        var comments: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            /** Fields to be accounted for:
            val title: String,
            val link: String,
            val description: String,
            val author: String,
            val guid: String,
            val pubDate: String,
            val content: String,
            val comments: String
             */
            // Log.e("readItem", parser.name)
            when(parser.name) {
                "title" -> title = readText(parser, "title")
                "link" -> link = readText(parser, "link")
                "description" -> description = readText(parser, "description")
                "author" -> author = readText(parser, "author")
                "guid" -> guid = readText(parser, "guid")
                "pubDate" -> pubDate = readText(parser, "pubDate")
                "content:encoded" -> content = readText(parser, "content:encoded")
                "comments" -> comments = readText(parser, "comments")
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "item")
        return ChocolateyItem(
            guid!!,
            title!!,
            link,
            description,
            author,
            pubDate!!,
            content!!,
            comments
        )
    }

    /**
     * Reads the text from a specific node.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return result
    }
}