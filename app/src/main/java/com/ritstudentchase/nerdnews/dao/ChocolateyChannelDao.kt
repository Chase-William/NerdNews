package com.ritstudentchase.nerdnews.dao

import androidx.room.*
import com.ritstudentchase.nerdnews.models.*

@Dao
interface ChocolateyChannelDao {
    @Query("SELECT * FROM $CHOCOLATEY_CHANNEL_TABLE_NAME")
    fun getAll(): List<ChocolateyChannel>

    @Update
    fun update(channel: ChocolateyChannel)

    @Insert
    fun insert(item: ChocolateyChannel)
}