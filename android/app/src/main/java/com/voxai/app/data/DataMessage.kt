package com.voxai.app.data

import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Data channel message types sent from the Python LiveKit Agent to the Android client.
 * Topic: "voxai"
 */
sealed class DataMessage {
    abstract val type: String

    /** Real-time interim user speech recognition (partial results) */
    data class InterimTranscript(
        val text: String,
        val isFinal: Boolean,
        val timestamp: Double,
    ) : DataMessage() {
        override val type = "interim_transcript"
    }

    /** Final transcript — user input or AI response */
    data class FinalTranscript(
        val speaker: String,    // "user" or "agent"
        val text: String,
        val id: String,
        val timestamp: Double,
    ) : DataMessage() {
        override val type = "final_transcript"
    }

    /** Agent state change (listening, thinking, speaking) */
    data class AgentStateChange(
        val agentState: String,
        val userState: String,
        val timestamp: Double,
    ) : DataMessage() {
        override val type = "agent_state"
    }

    companion object {
        private val gson = Gson()

        fun fromJson(jsonString: String): DataMessage? {
            return try {
                val obj = gson.fromJson(jsonString, JsonObject::class.java)
                val type = obj.get("type")?.asString ?: return null

                when (type) {
                    "interim_transcript" -> InterimTranscript(
                        text = obj.get("text")?.asString ?: "",
                        isFinal = obj.get("is_final")?.asBoolean ?: false,
                        timestamp = obj.get("timestamp")?.asDouble ?: System.currentTimeMillis() / 1000.0,
                    )
                    "final_transcript" -> FinalTranscript(
                        speaker = obj.get("speaker")?.asString ?: "agent",
                        text = obj.get("text")?.asString ?: "",
                        id = obj.get("id")?.asString ?: "",
                        timestamp = obj.get("timestamp")?.asDouble ?: System.currentTimeMillis() / 1000.0,
                    )
                    "agent_state" -> AgentStateChange(
                        agentState = obj.get("agent_state")?.asString ?: "listening",
                        userState = obj.get("user_state")?.asString ?: "listening",
                        timestamp = obj.get("timestamp")?.asDouble ?: System.currentTimeMillis() / 1000.0,
                    )
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

/** A single transcript entry for display and persistence */
data class TranscriptEntry(
    val id: String,
    val speaker: String,    // "user" or "agent"
    val text: String,
    val timestamp: Long,
)
