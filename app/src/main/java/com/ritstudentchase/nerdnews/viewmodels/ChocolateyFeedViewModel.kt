package com.ritstudentchase.nerdnews.viewmodels

/**
 * https://developer.android.com/training/basics/network-ops/xml -- Used to learn
 */

import android.util.Log
import android.util.Xml
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
// import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import com.ritstudentchase.nerdnews.util.RssFeed
import com.ritstudentchase.nerdnews.util.RssFeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
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

const val CANNOT_FETCH_CONTENT_RIGHT_NOW_ERROR = "Unable to fetch new content at this time, please try again later."
const val UPDATED = "you're updated to date!"

class ChocolateyFeedViewModel(
    /**
     * Requires database access to manage remote (feed-items / channel) integration with local store.
     */
    database: NerdNewsDatabase
): RssFeedParser<ChocolateyChannel, ChocolateyItem>, RssFeed<ChocolateyItem> {
    private var itemsDao = database.chocolateyItemDao()
    private var channelDao = database.chocolateyChannelDao()

    // channel and items, load with initial database information
    private val channel: MutableState<ChocolateyChannel?> = mutableStateOf(null)
    private val items: MutableList<ChocolateyItem> = mutableStateListOf()

    fun getChannel() = channel
    override fun getItems() = items

    // Should be abstracted because it is the same as MicrosoftFeedViewModel
    override suspend fun loadLocal() {
        try {
            // channel.value = channelDao.getAll().firstOrNull()
            itemsDao.getAll().collect {
                if (it != null)
                    if (it.isNotEmpty())
                        for (item in it) {
                            // Only add local items that don't already exist in the collection
                            if (items.all { feedItem -> feedItem.guid != item.guid })
                                items.add(item)
                            else // Save new items
                                itemsDao.insertAll(item)
                        }
            }
        }
        catch(ex: Exception) {
            Log.d("error", "")
        }
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
     override fun readItem(parser: XmlPullParser): ChocolateyItem {
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

    // Should be abstracted because it is the same as MicrosoftFeedViewModel
    override fun merge(): RequestResult {
        if (channel.value == null) {
            channel.value = channelDao.getAll().firstOrNull()
        }

        return getRemoteFeedItems() { incomingChannel, newItems ->
            // Handle Channel
            if (channel.value != null) {
                if (channel.value != incomingChannel) {
                    channel.value = incomingChannel
                    channelDao.update(incomingChannel!!)
                }
            } else {
                channel.value = incomingChannel
                channelDao.insert(incomingChannel!!)
            }

            // Handle Items
            items.addAll(newItems)
        }
    }

    /**
     * Handles the parsing of a channel.
     */
    override fun readChannel(parser: XmlPullParser, itemName: String): Pair<ChocolateyChannel, List<ChocolateyItem>> {
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
            if (parser.name == itemName) {
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
}