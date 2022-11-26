package com.ritstudentchase.nerdnews.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CHOCOLATEY_ITEM_TABLE_NAME = "chocolatey_item"

/**
 * Represents a blog item that exist within the channel.
 */
@Entity(tableName = CHOCOLATEY_ITEM_TABLE_NAME)
data class ChocolateyItem(
    @PrimaryKey val guid: String,
    @ColumnInfo val title: String,
    @ColumnInfo val link: String?,
    @ColumnInfo val description: String?,
    @ColumnInfo val author: String?,
    @ColumnInfo val pubDate: String,
    @ColumnInfo val content: String,
    @ColumnInfo val comments: String?
)