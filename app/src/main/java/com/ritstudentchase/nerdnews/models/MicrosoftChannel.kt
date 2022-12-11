package com.ritstudentchase.nerdnews.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val MICROSOFT_CHANNEL_TABLE_NAME = "microsoft_channel"

@Entity(tableName = MICROSOFT_CHANNEL_TABLE_NAME)
data class MicrosoftChannel(
    @ColumnInfo val title: String,
    @PrimaryKey val link: String,
    @ColumnInfo val description: String,
    @ColumnInfo val language: String,
    @ColumnInfo val copyright: String,
    @ColumnInfo val generator: String
)

