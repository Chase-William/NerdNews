package com.ritstudentchase.nerdnews.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ritstudentchase.nerdnews.dao.NerdNewsDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import com.ritstudentchase.nerdnews.models.MicrosoftChannel
import com.ritstudentchase.nerdnews.models.MicrosoftItem
import com.ritstudentchase.nerdnews.util.RssFeed
import com.ritstudentchase.nerdnews.util.RssFeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.lang.Exception

class MicrosoftFeedViewModel(
    database: NerdNewsDatabase
) : ViewModel(), RssFeedParser<MicrosoftChannel, MicrosoftItem>, RssFeed<MicrosoftItem> {
    private var itemsDao = database.microsoftItemDao()
    private var channelDao = database.microsoftChannelDao()

    // channel and items, load with initial database information
    private val channel: MutableState<MicrosoftChannel?> = mutableStateOf(null)
    private val items: MutableList<MicrosoftItem> = mutableStateListOf()

    override suspend fun loadLocal() {
        try {
            itemsDao.getAll().collect {
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
            Log.d("error", "asd")
        }
    }

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

    override fun getItems() = items

    override fun readChannel(
        parser: XmlPullParser,
        itemName: String
    ): Pair<MicrosoftChannel, List<MicrosoftItem>>? {
        Log.d("Microsoft", "Reading RSS Channel")
        parser.require(XmlPullParser.START_TAG, null, "channel")
        val items = mutableListOf<MicrosoftItem>()
        // parse channel
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var language: String? = null
        var copyright: String? = null
        var generator: String? = null

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
                "language" -> description = readText(parser, "language")
                "copyright" -> copyright = readText(parser, "copyright")
                "generator" -> generator = readText(parser, "managingEditor")
            }
            // Read items of the channel, items themselves are structures
            if (parser.name == itemName) {
                items.add(readItem(parser))
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "channel")
        return Pair(
            MicrosoftChannel(
                title!!,
                link!!,
                description!!,
                language!!,
                copyright!!,
                generator!!),
            items
        )
    }

    override fun readItem(parser: XmlPullParser): MicrosoftItem {
        Log.d("Chocolatey", "Reading RSS Item")
        parser.require(XmlPullParser.START_TAG, null, "item")
        var guid: String? = null
        var link: String? = null
        var authorName: String? = null
        var authorUri: String? = null
        var title: String? = null
        var description: String? = null
        var updated: String? = null

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
                "guid" -> guid = readText(parser, "guid")
                "link" -> link = readText(parser, "link")
                "authorName" -> authorName = readText(parser, "authorName")
                "authorUri" -> authorUri = readText(parser, "authorUri")
                "title" -> title = readText(parser, "title")
                "description" -> description = readText(parser, "description")
                "updated" -> updated = readText(parser, "updated")
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "item")
        return MicrosoftItem(
            guid!!,
            link!!,
            authorName!!,
            authorUri!!,
            title!!,
            description,
            updated!!
        )
    }
}