package com.ritstudentchase.nerdnews.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.models.ChocolateyItem
import com.ritstudentchase.nerdnews.models.MicrosoftChannel
import com.ritstudentchase.nerdnews.models.MicrosoftItem

@Database(
    entities = [
        ChocolateyItem::class,
        ChocolateyChannel::class,
        MicrosoftItem::class,
        MicrosoftChannel::class],
    version = 1,
    exportSchema = false)
abstract class NerdNewsDatabase : RoomDatabase() {
    abstract fun chocolateyItemDao(): ChocolateyItemDao
    abstract fun chocolateyChannelDao(): ChocolateyChannelDao
    abstract fun microsoftItemDao(): MicrosoftItemDao
    abstract fun microsoftChannelDao(): MicrosoftChannelDao


    companion object {
        @Volatile
        private var INSTANCE: NerdNewsDatabase? = null

        fun getDatabase(context: Context): NerdNewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    NerdNewsDatabase::class.java,
                    "nerdnews_db")
                    // .createFromAsset("databases/nerdnews")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}