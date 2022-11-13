/**
 * https://developer.android.com/training/basics/network-ops/xml -- Used to learn
 */

package com.ritstudentchase.nerdnews

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ChocolateyFeedService {

    /**
     * Makes a web-request to get an input stream containing Chocolately's RSS feed content.
     */
    fun getRSSFeed(): ChocolateyChannel? {
        val url = URL("https://feeds.feedburner.com/ChocolateyBlog")
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            val stream = BufferedInputStream(urlConnection.getInputStream())
            val channel = parse(stream)
            return channel
        } finally {
            urlConnection.disconnect()
        }
    }

    /**
     * Parses an input stream containing Chocolately RSS feed content.
     */
    private fun parse(inputStream: InputStream): ChocolateyChannel? {
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
    private fun readFeed(parser: XmlPullParser): ChocolateyChannel? {
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
    private fun readChannel(parser: XmlPullParser): ChocolateyChannel {
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
        return ChocolateyChannel(
            title,
            link,
            description,
            copyright,
            managingEditor,
            pubDate,
            lastBuildDate,
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
            title,
            link,
            description,
            author,
            guid,
            pubDate,
            content,
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

/**
 * Represents a channel that contains items (chocolately blogs).
 */
data class ChocolateyChannel(
    val title: String?,
    val link: String?,
    val description: String?,
    val copyright: String?,
    val managingEditor: String?,
    val pubDate: String?,
    val lastBuildDate: String?,
    val items: MutableList<ChocolateyItem>?
)

/**
 * Represents a blog item that exist within the channel.
 */
data class ChocolateyItem(
    val title: String?,
    val link: String?,
    val description: String?,
    val author: String?,
    val guid: String?,
    val pubDate: String?,
    val content: String?,
    val comments: String?
)



//    private fun readTitle(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "title")
//        val title = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "title")
//        return title
//    }
//    private fun readLink(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "link")
//        val link = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "link")
//        return link
//    }
//    private fun readDescription(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "description")
//        val description = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "description")
//        return description
//    }
//    private fun readCopyright(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "copyright")
//        val copyright = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "copyright")
//        return copyright
//    }
//    private fun readManagingEditor(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "managingEditor")
//        val managingEditor = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "managingEditor")
//        return managingEditor
//    }
//    private fun readPubDate(parser: XmlPullParser): String {
//        parser.require(XmlPullParser.START_TAG, null, "pubDate")
//        val pubDate = readText(parser)
//        parser.require(XmlPullParser.END_TAG, null, "pubDate")
//        return pubDate
//    }
////    private fun read(parser: XmlPullParser): String {
////        parser.require(XmlPullParser.START_TAG, null, )
////        parser.require(XmlPullParser.END_TAG, null, )
////    }
//
//    @Throws(IOException::class, XmlPullParserException::class)
//    private fun readText(parser: XmlPullParser): String {
//        var result = ""
//        if (parser.next() == XmlPullParser.TEXT) {
//            result = parser.text
//            parser.nextTag()
//        }
//        return result
//    }