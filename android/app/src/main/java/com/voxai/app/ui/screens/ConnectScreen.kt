package com.voxai.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxai.app.ui.components.AudioWaveform
import com.voxai.app.ui.components.GlassCard
import com.voxai.app.ui.theme.NeonBlack
import com.voxai.app.ui.theme.NeonBorder
import com.voxai.app.ui.theme.NeonCyan
import com.voxai.app.ui.theme.NeonGradient
import com.voxai.app.ui.theme.NeonGreen
import com.voxai.app.ui.theme.NeonGray
import com.voxai.app.ui.theme.NeonPurple
import com.voxai.app.ui.theme.NeonSurface
import com.voxai.app.ui.theme.NeonWhite
import com.voxai.app.viewmodel.CallViewModel

@Composable
fun ConnectScreen(
    viewModel: CallViewModel,
    onConnect: (room: String, user: String) -> Unit,
    onHistory: () -> Unit,
) {
    var roomInput by remember { mutableStateOf(viewModel.roomName.value) }
    var userInput by remember { mutableStateOf(viewModel.userName.value) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsUrl by remember { mutableStateOf(viewModel.livekitUrl.value) }
    var settingsKey by remember { mutableStateOf(viewModel.apiKey.value) }
    var settingsSecret by remember { mutableStateOf(viewModel.apiSecret.value) }
    var settingsAgent by remember { mutableStateOf(viewModel.agentName.value) }

    val canConnect = viewModel.savedToken.value.isNotBlank() || viewModel.apiKey.value.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBlack)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { showSettings = !showSettings }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = NeonGray,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "v1.0.0",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = NeonGray,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logo section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Animated logo
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Glow
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                    )
                    // Card with waveform
                    GlassCard(
                        modifier = Modifier.size(96.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            AudioWaveform(
                                barCount = 5,
                                isActive = true,
                                modifier = Modifier.height(36.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "VoxAI",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = NeonGradient,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "实时 AI 语音助手",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = NeonGray,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Powered by LiveKit badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Powered by LiveKit",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input fields
            NeonTextField(
                value = roomInput,
                onValueChange = { roomInput = it },
                placeholder = "房间名称（可选）",
            )
            Spacer(modifier = Modifier.height(12.dp))
            NeonTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = "你的昵称（可选）",
                imeAction = ImeAction.Done,
                onDone = { onConnect(roomInput, userInput) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Connect button
            NeonButton(
                text = "开始语音通话",
                icon = Icons.Default.Phone,
                onClick = { onConnect(roomInput, userInput) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            // History button
            GlassButton(
                text = "通话历史",
                icon = Icons.Default.History,
                onClick = onHistory,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feature hints
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeatureHint(Icons.Default.AutoAwesome, "AI 对话")
                Spacer(modifier = Modifier.width(16.dp))
                FeatureHint(Icons.Default.Phone, "实时通话")
                Spacer(modifier = Modifier.width(16.dp))
                FeatureHint(Icons.Default.History, "历史记录")
            }
        }

        // Settings panel
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        NeonSurface,
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, NeonBorder.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = "连接设置",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonWhite,
                        ),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NeonTextField(
                        value = settingsUrl,
                        onValueChange = { settingsUrl = it },
                        placeholder = "LiveKit 服务器 URL",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonTextField(
                        value = settingsKey,
                        onValueChange = { settingsKey = it },
                        placeholder = "API Key",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonTextField(
                        value = settingsSecret,
                        onValueChange = { settingsSecret = it },
                        placeholder = "API Secret",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonTextField(
                        value = settingsAgent,
                        onValueChange = { settingsAgent = it },
                        placeholder = "Agent 名称（如 voxai-agent）",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NeonButton(
                        text = "保存",
                        icon = Icons.Default.Settings,
                        onClick = {
                            viewModel.saveSettings(settingsUrl, settingsKey, settingsSecret)
                            viewModel.saveAgentName(settingsAgent)
                            showSettings = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NeonSurface.copy(alpha = 0.6f))
            .border(1.dp, NeonBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, color = NeonGray.copy(alpha = 0.5f)),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = NeonWhite),
            cursorBrush = SolidColor(NeonCyan),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NeonButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NeonGradient)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
    }
}

@Composable
private fun GlassButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NeonSurface.copy(alpha = 0.5f))
            .border(1.dp, NeonBorder.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NeonGray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NeonWhite))
        }
    }
}

@Composable
private fun FeatureHint(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = NeonCyan.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = TextStyle(fontSize = 11.sp, color = NeonGray))
    }
}
