package com.voxai.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voxai.app.data.AppDatabase
import com.voxai.app.data.AppPreferences
import com.voxai.app.data.CallHistoryEntity
import com.voxai.app.data.LiveKitManager
import com.voxai.app.data.TokenGenerator
import com.voxai.app.data.TranscriptEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallViewModel(app: Application) : AndroidViewModel(app) {

    init {
        Log.d("CallViewModel", "Constructor start")
    }

    private val prefs = AppPreferences(app).also {
        Log.d("CallViewModel", "AppPreferences created")
    }
    private val db = AppDatabase.getInstance(app).also {
        Log.d("CallViewModel", "AppDatabase created")
    }
    private val liveKit = LiveKitManager(app).also {
        Log.d("CallViewModel", "LiveKitManager created")
    }

    // --- Settings ---
    val livekitUrl = MutableStateFlow(prefs.livekitUrl)
    val apiKey = MutableStateFlow(prefs.livekitApiKey)
    val apiSecret = MutableStateFlow(prefs.livekitApiSecret)
    val savedToken = MutableStateFlow(prefs.livekitToken)
    val agentName = MutableStateFlow(prefs.agentName)

    // --- Connection form ---
    val roomName = MutableStateFlow(prefs.defaultRoomName)
    val userName = MutableStateFlow(prefs.defaultUserName)

    // --- Call state (delegated from LiveKitManager) ---
    val connectionState = liveKit.connectionState
    val agentState = liveKit.agentState
    val userState = liveKit.userState
    val interimSubtitle = liveKit.interimSubtitle
    val transcript = liveKit.transcript
    val micMuted = liveKit.micMuted
    val speakerOn = liveKit.speakerOn
    val currentRoomName = liveKit.roomName

    // --- Call duration ---
    private val _callDuration = MutableStateFlow(0L) // seconds
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    private var durationJob: kotlinx.coroutines.Job? = null

    // --- History ---
    val callHistory = db.callHistoryDao().getAll().also {
        Log.d("CallViewModel", "callHistory initialized")
    }

    // --- Settings save ---
    fun saveSettings(url: String, key: String, secret: String) {
        prefs.livekitUrl = url
        prefs.livekitApiKey = key
        prefs.livekitApiSecret = secret
        livekitUrl.value = url
        apiKey.value = key
        apiSecret.value = secret
    }

    fun saveRoomName(name: String) {
        prefs.defaultRoomName = name
        roomName.value = name
    }

    fun saveUserName(name: String) {
        prefs.defaultUserName = name
        userName.value = name
    }

    fun saveAgentName(name: String) {
        prefs.agentName = name
        agentName.value = name
    }

    // --- Connection ---

    /**
     * Connect to a LiveKit room. Generates a token if API key/secret are available,
     * otherwise uses the saved token.
     */
    fun connectToRoom(room: String, user: String) {
        Log.d("CallViewModel", "connectToRoom called: room=$room, user=$user")
        
        val url = prefs.livekitUrl.trim()
        val roomName = room.ifBlank { "voice-${System.currentTimeMillis().toString(36).takeLast(6)}" }
        val identity = user.ifBlank { "user-${(1000..9999).random()}" }

        Log.d("CallViewModel", "URL: $url, roomName: $roomName, identity: $identity")
        Log.d("CallViewModel", "canGenerateToken: ${prefs.canGenerateToken()}, savedToken: ${prefs.livekitToken.isNotBlank()}")

        // Save defaults
        saveRoomName(roomName)
        saveUserName(identity)

        // Generate or use token
        val token = if (prefs.canGenerateToken()) {
            Log.d("CallViewModel", "Generating token with apiKey=${prefs.livekitApiKey.trim().take(5)}...")
            val agentName = prefs.agentName.takeIf { it.isNotBlank() }
            TokenGenerator.generateToken(
                apiKey = prefs.livekitApiKey.trim(),
                apiSecret = prefs.livekitApiSecret.trim(),
                roomName = roomName,
                identity = identity,
                name = user.ifBlank { identity },
                agentName = agentName,
            )
        } else if (prefs.livekitToken.isNotBlank()) {
            Log.d("CallViewModel", "Using saved token")
            prefs.livekitToken.trim()
        } else {
            Log.e("CallViewModel", "No valid credentials!")
            // No valid credentials
            return
        }

        // Start duration timer
        startDurationTimer()

        // Log token for debugging
        Log.d("CallViewModel", "Connecting to room: $roomName with token: $token")

        // Connect
        liveKit.connect(url, token, roomName)
    }

    /**
     * Disconnect and save call history.
     */
    fun endCall() {
        stopDurationTimer()

        // Save to history
        val duration = _callDuration.value
        val transcriptList = transcript.value
        val roomNameStr = currentRoomName.value
        val userNameStr = userName.value.ifBlank { "我" }

        if (duration > 0) {
            viewModelScope.launch {
                val transcriptJson = com.google.gson.Gson().toJson(transcriptList)
                db.callHistoryDao().insert(
                    CallHistoryEntity(
                        roomName = roomNameStr,
                        userName = userNameStr,
                        startTime = System.currentTimeMillis() - duration * 1000,
                        duration = duration,
                        transcriptJson = transcriptJson,
                    )
                )
            }
        }

        _callDuration.value = 0
        liveKit.disconnect()
    }

    fun toggleMute() = liveKit.toggleMute()
    fun toggleSpeaker() = liveKit.toggleSpeaker()

    fun deleteHistory(id: Long) {
        viewModelScope.launch { db.callHistoryDao().deleteById(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { db.callHistoryDao().deleteAll() }
    }

    private fun startDurationTimer() {
        _callDuration.value = 0
        durationJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _callDuration.value += 1
            }
        }
    }

    private fun stopDurationTimer() {
        durationJob?.cancel()
        durationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        liveKit.release()
    }
}
