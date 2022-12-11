package com.ritstudentchase.nerdnews.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ritstudentchase.nerdnews.models.MICROSOFT_CHANNEL_TABLE_NAME
import com.ritstudentchase.nerdnews.models.MicrosoftChannel

@Dao
interface MicrosoftChannelDao {
    @Query("SELECT * FROM $MICROSOFT_CHANNEL_TABLE_NAME")
    fun getAll(): List<MicrosoftChannel>

    @Update
    fun update(channel: MicrosoftChannel)

    @Insert
    fun insert(item: MicrosoftChannel)
}