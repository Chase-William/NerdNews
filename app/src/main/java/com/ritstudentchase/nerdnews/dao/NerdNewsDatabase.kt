package com.ritstudentchase.nerdnews.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ritstudentchase.nerdnews.models.ChocolateyChannel
import com.ritstudentchase.nerdnews.models.ChocolateyItem

@Database(entities = [ChocolateyItem::class, ChocolateyChannel::class], version = 1, exportSchema = false)
abstract class NerdNewsDatabase : RoomDatabase() {
    abstract fun chocolateyItemDao(): ChocolateyItemDao
    abstract fun chocolateyChannelDao(): ChocolateyChannelDao


    companion object {
        @Volatile
        private var INSTANCE: NerdNewsDatabase? = null

        fun getDatabase(context: Context): NerdNewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    NerdNewsDatabase::class.java,
                    "app_database")
                    // .createFromAsset("databases/nerdnews")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}