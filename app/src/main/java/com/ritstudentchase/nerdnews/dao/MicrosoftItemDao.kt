package com.ritstudentchase.nerdnews.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ritstudentchase.nerdnews.models.MICROSOFT_ITEM_TABLE_NAME
import com.ritstudentchase.nerdnews.models.MicrosoftItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MicrosoftItemDao {
    @Query("SELECT * FROM $MICROSOFT_ITEM_TABLE_NAME")
    fun getAll(): Flow<List<MicrosoftItem>>

    @Update
    fun update(item: MicrosoftItem)
    @Update
    fun update(items: List<MicrosoftItem>)

    @Insert
    fun insertAll(items: List<MicrosoftItem>)
    @Insert
    fun insertAll(vararg items: MicrosoftItem)
}