package com.voxai.app.data

import android.content.Context
import android.util.Log
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages the LiveKit room connection lifecycle, audio, and data channel.
 *
 * Emits state changes and transcript messages via StateFlows that the
 * ViewModel and UI can observe.
 */
class LiveKitManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var room: Room? = null
    private var eventJob: Job? = null

    // --- Observable state ---

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Agent states from the server
    private val _agentState = MutableStateFlow("initializing")
    val agentState: StateFlow<String> = _agentState.asStateFlow()

    private val _userState = MutableStateFlow("listening")
    val userState: StateFlow<String> = _userState.asStateFlow()

    // Current interim subtitle (real-time, partial)
    private val _interimSubtitle = MutableStateFlow<TranscriptEntry?>(null)
    val interimSubtitle: StateFlow<TranscriptEntry?> = _interimSubtitle.asStateFlow()

    // Completed transcript entries
    private val _transcript = MutableStateFlow<List<TranscriptEntry>>(emptyList())
    val transcript: StateFlow<List<TranscriptEntry>> = _transcript.asStateFlow()

    // Microphone mute state
    private val _micMuted = MutableStateFlow(false)
    val micMuted: StateFlow<Boolean> = _micMuted.asStateFlow()

    // Speaker state
    private val _speakerOn = MutableStateFlow(true)
    val speakerOn: StateFlow<Boolean> = _speakerOn.asStateFlow()

    // Room info
    private val _roomName = MutableStateFlow("")
    val roomName: StateFlow<String> = _roomName.asStateFlow()

    /**
     * Connect to a LiveKit room.
     */
    fun connect(url: String, token: String, roomName: String) {
        if (_connectionState.value is ConnectionState.Connecting) return

        _connectionState.value = ConnectionState.Connecting
        _roomName.value = roomName
        _transcript.value = emptyList()
        _interimSubtitle.value = null
        _agentState.value = "initializing"
        _userState.value = "listening"

        scope.launch {
            try {
                // Create room with options
                val roomInstance = LiveKit.create(
                    appContext = context.applicationContext,
                    options = RoomOptions(
                        adaptiveStream = false,
                        dynacast = false,
                    )
                )
                room = roomInstance

                // Connect to the room
                roomInstance.connect(
                    url = url,
                    token = token,
                )

                // Enable microphone
                roomInstance.localParticipant.setMicrophoneEnabled(true)
                _micMuted.value = false

                // Start collecting room events
                eventJob = scope.launch {
                    roomInstance.events.collect { event ->
                        handleRoomEvent(event)
                    }
                }

                _connectionState.value = ConnectionState.Connected
                Log.d(TAG, "Connected to room: $roomName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                _connectionState.value = ConnectionState.Error(e.message ?: "连接失败")
            }
        }
    }

    /**
     * Disconnect from the current room.
     */
    fun disconnect() {
        scope.launch {
            try {
                eventJob?.cancel()
                eventJob = null
                room?.disconnect()
                room?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error during disconnect", e)
            } finally {
                room = null
                _connectionState.value = ConnectionState.Disconnected
                _interimSubtitle.value = null
                _agentState.value = "initializing"
                _userState.value = "listening"
            }
        }
    }

    /**
     * Toggle microphone mute.
     */
    fun toggleMute() {
        val newMuted = !_micMuted.value
        scope.launch {
            try {
                room?.localParticipant?.setMicrophoneEnabled(!newMuted)
                _micMuted.value = newMuted
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle mute", e)
            }
        }
    }

    /**
     * Toggle speaker output.
     */
    fun toggleSpeaker() {
        _speakerOn.value = !_speakerOn.value
        // The LiveKit SDK handles audio routing automatically.
        // Speaker toggle affects the audio output routing.
        try {
            room?.let { r ->
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                audioManager.isSpeakerphoneOn = _speakerOn.value
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle speaker", e)
        }
    }

    /**
     * Handle incoming room events.
     */
    private fun handleRoomEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.DataReceived -> {
                // Parse data message from the agent
                val payload = event.data?.toString(Charsets.UTF_8) ?: return
                val topic = event.topic ?: return

                if (topic != DATA_TOPIC) return

                val message = DataMessage.fromJson(payload) ?: return

                when (message) {
                    is DataMessage.InterimTranscript -> {
                        _interimSubtitle.value = TranscriptEntry(
                            id = "interim",
                            speaker = "user",
                            text = message.text,
                            timestamp = (message.timestamp * 1000).toLong(),
                        )
                        if (message.isFinal) {
                            _interimSubtitle.value = null
                        }
                    }

                    is DataMessage.FinalTranscript -> {
                        // Add to transcript list
                        val entry = TranscriptEntry(
                            id = message.id.ifEmpty { System.currentTimeMillis().toString() },
                            speaker = message.speaker,
                            text = message.text,
                            timestamp = (message.timestamp * 1000).toLong(),
                        )
                        _transcript.value = _transcript.value + entry
                        // Clear interim if it was the user's final
                        if (message.speaker == "user") {
                            _interimSubtitle.value = null
                        }
                    }

                    is DataMessage.AgentStateChange -> {
                        _agentState.value = message.agentState
                        _userState.value = message.userState
                    }
                }
            }

            is RoomEvent.ParticipantConnected -> {
                Log.d(TAG, "Participant connected: ${event.participant.identity}")
            }

            is RoomEvent.ParticipantDisconnected -> {
                Log.d(TAG, "Participant disconnected: ${event.participant.identity}")
            }

            is RoomEvent.Disconnected -> {
                _connectionState.value = ConnectionState.Disconnected
                Log.d(TAG, "Room disconnected")
            }

            else -> {
                // Ignore other events
            }
        }
    }

    fun release() {
        disconnect()
    }

    companion object {
        private const val TAG = "LiveKitManager"
        private const val DATA_TOPIC = "voxai"
    }
}
