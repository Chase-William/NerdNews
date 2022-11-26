package com.ritstudentchase.nerdnews.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ritstudentchase.nerdnews.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChocolateyItemDao {
    @Query("SELECT * FROM $CHOCOLATEY_ITEM_TABLE_NAME")
    fun getAll(): Flow<List<ChocolateyItem>>

    @Update
    fun update(item: ChocolateyItem)

    @Insert
    fun insertAll(items: List<ChocolateyItem>)
    @Insert
    fun insertAll(vararg items: ChocolateyItem)
}