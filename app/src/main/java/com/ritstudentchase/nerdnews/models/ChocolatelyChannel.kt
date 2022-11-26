package com.ritstudentchase.nerdnews.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CHOCOLATEY_CHANNEL_TABLE_NAME = "chocolatey_channel"

/**
 * Represents a channel that contains items (chocolately blogs).
 */
@Entity(tableName = CHOCOLATEY_CHANNEL_TABLE_NAME)
data class ChocolateyChannel(
    @PrimaryKey val link: String,
    @ColumnInfo val title: String,
    @ColumnInfo val pubDate: String,
    @ColumnInfo val description: String,
    @ColumnInfo val copyright: String,
    @ColumnInfo val managingEditor: String,
    @ColumnInfo val lastBuildDate: String
)