package com.example.mindscale.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wellness_entries")
data class WellnessEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val intensity: Int?,
    val timestamp: Long,
    val type: String, // "depression", "sleep", or "wake"
    val note: String? = null
)