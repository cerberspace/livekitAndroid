package com.voxai.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxai.app.data.LiveKitManager
import com.voxai.app.data.TranscriptEntry
import com.voxai.app.ui.components.AudioWaveform
import com.voxai.app.ui.components.GlassCard
import com.voxai.app.ui.theme.NeonBlack
import com.voxai.app.ui.theme.NeonBorder
import com.voxai.app.ui.theme.NeonCyan
import com.voxai.app.ui.theme.NeonGradient
import com.voxai.app.ui.theme.NeonGreen
import com.voxai.app.ui.theme.NeonGray
import com.voxai.app.ui.theme.NeonPurple
import com.voxai.app.ui.theme.NeonRed
import com.voxai.app.ui.theme.NeonSurface
import com.voxai.app.ui.theme.NeonWhite
import com.voxai.app.viewmodel.CallViewModel

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onEndCall: () -> Unit,
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val agentState by viewModel.agentState.collectAsState()
    val userState by viewModel.userState.collectAsState()
    val interimSubtitle by viewModel.interimSubtitle.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val micMuted by viewModel.micMuted.collectAsState()
    val speakerOn by viewModel.speakerOn.collectAsState()
    val roomName by viewModel.currentRoomName.collectAsState()
    val duration by viewModel.callDuration.collectAsState()

    val isAgentSpeaking = agentState == "speaking"
    val isUserSpeaking = userState == "speaking"
    val isWaveformActive = isAgentSpeaking || isUserSpeaking

    val listState = rememberLazyListState()

    // Auto-scroll transcript
    LaunchedEffect(transcript.size) {
        if (transcript.isNotEmpty()) {
            listState.animateScrollToItem(transcript.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeonPurple.copy(alpha = 0.05f),
                        NeonBlack,
                        NeonBlack,
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = roomName,
                    style = TextStyle(fontSize = 11.sp, color = NeonGray),
                )
                Text(
                    text = formatDuration(duration),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonWhite,
                    ),
                )
            }
            // Status badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NeonGreen.copy(alpha = 0.1f))
                    .border(1.dp, NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (connectionState) {
                        is LiveKitManager.ConnectionState.Connected -> "已连接"
                        is LiveKitManager.ConnectionState.Connecting -> "连接中..."
                        is LiveKitManager.ConnectionState.Error -> "连接错误"
                        else -> "未连接"
                    },
                    style = TextStyle(fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Medium),
                )
            }
        }

        // Agent avatar + waveform
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Pulse rings
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isWaveformActive) {
                    PulseRing(
                        modifier = Modifier.size(160.dp),
                        color = NeonCyan.copy(alpha = 0.2f),
                        delay = 0,
                    )
                    PulseRing(
                        modifier = Modifier.size(160.dp),
                        color = NeonPurple.copy(alpha = 0.2f),
                        delay = 500,
                    )
                }

                // Glow
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isAgentSpeaking) NeonCyan.copy(alpha = 0.4f) else NeonPurple.copy(alpha = 0.3f),
                                    Color.Transparent,
                                )
                            )
                        )
                )

                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(NeonGradient)
                        .border(2.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // Agent icon — a stylized voice wave
                    AudioWaveform(
                        barCount = 5,
                        isActive = isWaveformActive,
                        modifier = Modifier.height(36.dp),
                        barColor = Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.7f))),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agent name
            Text(
                text = "VoxAI 助手",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonWhite),
            )

            // State indicator
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isAgentSpeaking -> NeonCyan
                                isUserSpeaking -> NeonPurple
                                else -> NeonGray
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isAgentSpeaking -> "正在回复..."
                        isUserSpeaking -> "正在聆听..."
                        else -> "就绪"
                    },
                    style = TextStyle(fontSize = 12.sp, color = NeonGray),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audio waveform visualization
            AudioWaveform(
                barCount = 32,
                isActive = isWaveformActive,
                modifier = Modifier.height(56.dp),
            )
        }

        // Subtitle area (current/interim)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(80.dp),
        ) {
            interimSubtitle?.let { subtitle ->
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    borderColor = NeonPurple,
                ) {
                    Column {
                        Text(
                            text = if (subtitle.speaker == "agent") "AI 助手" else "你",
                            style = TextStyle(fontSize = 10.sp, color = NeonGray),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle.text,
                            style = TextStyle(fontSize = 13.sp, color = NeonWhite, lineHeight = 18.sp),
                        )
                    }
                }
            } ?: run {
                // Show last agent message as subtitle if no interim
                val lastAgent = transcript.lastOrNull { it.speaker == "agent" }
                if (lastAgent != null && isAgentSpeaking) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        borderColor = NeonCyan,
                    ) {
                        Column {
                            Text(
                                text = "AI 助手",
                                style = TextStyle(fontSize = 10.sp, color = NeonGray),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastAgent.text,
                                style = TextStyle(fontSize = 13.sp, color = NeonWhite, lineHeight = 18.sp),
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "等待对话...",
                            style = TextStyle(fontSize = 12.sp, color = NeonGray.copy(alpha = 0.5f)),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transcript log
        if (transcript.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(72.dp),
            ) {
                items(transcript.takeLast(5)) { entry ->
                    TranscriptRow(entry)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mute button
            ControlButton(
                icon = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                isActive = !micMuted,
                isDanger = micMuted,
                onClick = { viewModel.toggleMute() },
            )

            Spacer(modifier = Modifier.width(20.dp))

            // End call button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NeonRed)
                    .clickable { onEndCall() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "结束通话", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Speaker button
            ControlButton(
                icon = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                isActive = speakerOn,
                onClick = { viewModel.toggleSpeaker() },
            )
        }
    }
}

@Composable
private fun PulseRing(
    modifier: Modifier = Modifier,
    color: Color,
    delay: Long,
) {
    val transition = rememberInfiniteTransition(label = "pulse_$delay")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scale_$delay",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "alpha_$delay",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean = true,
    isDanger: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                if (isDanger) NeonRed.copy(alpha = 0.15f)
                else NeonSurface.copy(alpha = 0.6f)
            )
            .border(
                1.dp,
                if (isDanger) NeonRed.copy(alpha = 0.3f) else NeonBorder.copy(alpha = 0.2f),
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDanger) NeonRed else if (isActive) NeonWhite else NeonGray,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun TranscriptRow(entry: TranscriptEntry) {
    val isAgent = entry.speaker == "agent"
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isAgent) "AI:" else "我:",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAgent) NeonCyan else NeonPurple,
            ),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = entry.text,
            style = TextStyle(fontSize = 11.sp, color = NeonGray, lineHeight = 14.sp),
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
