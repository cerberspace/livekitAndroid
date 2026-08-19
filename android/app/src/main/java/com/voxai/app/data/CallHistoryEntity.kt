package com.voxai.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomName: String,
    val userName: String,
    val startTime: Long,
    val duration: Long,          // seconds
    val transcriptJson: String, // serialized list of TranscriptEntry
    val createdAt: Long = System.currentTimeMillis(),
)

class TranscriptConverter {
    @TypeConverter
    fun fromTranscriptList(list: List<TranscriptEntry>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toTranscriptList(json: String): List<TranscriptEntry> {
        val type = object : TypeToken<List<TranscriptEntry>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }
}
