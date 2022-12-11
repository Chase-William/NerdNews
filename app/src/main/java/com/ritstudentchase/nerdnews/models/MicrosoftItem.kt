package com.ritstudentchase.nerdnews.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val MICROSOFT_ITEM_TABLE_NAME = "microsoft_item"

@Entity(tableName = MICROSOFT_ITEM_TABLE_NAME)
data class MicrosoftItem(
    @PrimaryKey val guid: String,
    @ColumnInfo val link: String?,
    @ColumnInfo val authorName: String?,
    @ColumnInfo val authorURI: String?,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String?,
    @ColumnInfo val updated: String
)