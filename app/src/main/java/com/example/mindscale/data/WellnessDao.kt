package com.example.mindscale.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WellnessEntry)

    @Update
    suspend fun update(entry: WellnessEntry)

    @Delete
    suspend fun delete(entry: WellnessEntry)

    @Query("SELECT * FROM wellness_entries ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEntries(): Flow<List<WellnessEntry>>
}