package com.example.mindscale.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EntryType {
    NORMAL,
    SLEEP,
    WAKE
}

@Entity(tableName = "wellness_entries")
data class WellnessEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val intensity: Int,
    val timestamp: Long,
    val entryType: EntryType,
    // Add the new optional note field
    val note: String? = null
)