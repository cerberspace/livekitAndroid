package com.voxai.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app preferences — LiveKit server connection settings.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("voxai_prefs", Context.MODE_PRIVATE)

    // LiveKit server connection
    var livekitUrl: String
        get() = prefs.getString(KEY_LIVEKIT_URL, "wss://your-livekit-server") ?: "wss://your-livekit-server"
        set(value) = prefs.edit().putString(KEY_LIVEKIT_URL, value).apply()

    var livekitApiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var livekitApiSecret: String
        get() = prefs.getString(KEY_API_SECRET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_SECRET, value).apply()

    // Default user/room
    var defaultUserName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var defaultRoomName: String
        get() = prefs.getString(KEY_ROOM_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ROOM_NAME, value).apply()

    // Agent dispatch name (empty = no agent dispatch)
    var agentName: String
        get() = prefs.getString(KEY_AGENT_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AGENT_NAME, value).apply()

    // Token (if using pre-generated token instead of API key/secret)
    var livekitToken: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    /** Check if we have enough info to connect */
    fun canConnect(): Boolean {
        return livekitUrl.isNotBlank() && livekitToken.isNotBlank()
    }

    /** Check if we can generate tokens (API key + secret) */
    fun canGenerateToken(): Boolean {
        return livekitUrl.isNotBlank() && livekitApiKey.isNotBlank() && livekitApiSecret.isNotBlank()
    }

    companion object {
        private const val KEY_LIVEKIT_URL = "livekit_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_SECRET = "api_secret"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ROOM_NAME = "room_name"
        private const val KEY_AGENT_NAME = "agent_name"
        private const val KEY_TOKEN = "token"
    }
}
