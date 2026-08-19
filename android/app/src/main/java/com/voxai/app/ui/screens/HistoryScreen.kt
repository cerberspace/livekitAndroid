package com.voxai.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.voxai.app.data.CallHistoryEntity
import com.voxai.app.data.TranscriptEntry
import com.voxai.app.ui.components.GlassCard
import com.voxai.app.ui.theme.NeonBlack
import com.voxai.app.ui.theme.NeonBorder
import com.voxai.app.ui.theme.NeonCyan
import com.voxai.app.ui.theme.NeonGradient
import com.voxai.app.ui.theme.NeonGray
import com.voxai.app.ui.theme.NeonPurple
import com.voxai.app.ui.theme.NeonRed
import com.voxai.app.ui.theme.NeonSurface
import com.voxai.app.ui.theme.NeonWhite
import com.voxai.app.viewmodel.CallViewModel

@Composable
fun HistoryScreen(
    viewModel: CallViewModel,
    onBack: () -> Unit,
) {
    val history by viewModel.callHistory.collectAsState(initial = emptyList())
    var selectedEntry by remember { mutableStateOf<CallHistoryEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    selectedEntry?.let { entry ->
        CallDetailScreen(
            entry = entry,
            onBack = { selectedEntry = null },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBlack)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonSurface.copy(alpha = 0.6f))
                    .border(1.dp, NeonBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = NeonWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("通话历史", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonWhite))
                Text("${history.size} 条记录", style = TextStyle(fontSize = 12.sp, color = NeonGray))
            }
            Spacer(modifier = Modifier.weight(1f))
            if (history.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonSurface.copy(alpha = 0.6f))
                        .border(1.dp, NeonBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { showClearConfirm = !showClearConfirm },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "清空", tint = NeonGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Clear confirm
        if (showClearConfirm) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                borderColor = NeonRed,
            ) {
                Column {
                    Text("确定要清空所有通话记录吗？", style = TextStyle(fontSize = 13.sp, color = NeonWhite))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonSurface.copy(alpha = 0.8f))
                                .border(1.dp, NeonBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { showClearConfirm = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("取消", style = TextStyle(fontSize = 13.sp, color = NeonWhite))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonRed)
                                .clickable {
                                    viewModel.clearHistory()
                                    showClearConfirm = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("清空", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }
        }

        // History list
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NeonSurface.copy(alpha = 0.4f))
                            .border(1.dp, NeonBorder.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = NeonGray.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无通话记录", style = TextStyle(fontSize = 14.sp, color = NeonGray))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("完成一次通话后，记录会显示在这里", style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.5f)))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
            ) {
                items(history, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onClick = { selectedEntry = entry },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: CallHistoryEntity,
    onClick: () -> Unit,
) {
    val transcriptList = parseTranscript(entry.transcriptJson)
    val firstMessage = transcriptList.firstOrNull()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        Row {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.roomName,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NeonWhite),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NeonGray.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }

                if (firstMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (firstMessage.speaker == "agent") "AI" else "我"}: ${firstMessage.text}",
                        style = TextStyle(fontSize = 12.sp, color = NeonGray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatDuration(entry.duration), style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.6f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("·", style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatRelativeTime(entry.startTime), style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.6f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("·", style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${transcriptList.size}条", style = TextStyle(fontSize = 11.sp, color = NeonGray.copy(alpha = 0.6f)))
                }
            }
        }
    }
}

@Composable
private fun CallDetailScreen(
    entry: CallHistoryEntity,
    onBack: () -> Unit,
) {
    val transcriptList = parseTranscript(entry.transcriptJson)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBlack)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonSurface.copy(alpha = 0.6f))
                    .border(1.dp, NeonBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = NeonWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("通话详情", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonWhite))
                Text(
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA).format(java.util.Date(entry.startTime)),
                    style = TextStyle(fontSize = 12.sp, color = NeonGray),
                )
            }
        }

        // Info card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(entry.roomName, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NeonWhite))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text("时长 ${formatDuration(entry.duration)}", style = TextStyle(fontSize = 11.sp, color = NeonGray))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${transcriptList.size} 条消息", style = TextStyle(fontSize = 11.sp, color = NeonGray))
                    }
                }
            }
        }

        // Transcript
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
        ) {
            items(transcriptList) { msg ->
                val isAgent = msg.speaker == "agent"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End,
                ) {
                    GlassCard(
                        modifier = Modifier.width(280.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        borderColor = if (isAgent) NeonCyan else NeonPurple,
                    ) {
                        Column {
                            Text(
                                if (isAgent) "AI 助手" else "我",
                                style = TextStyle(fontSize = 10.sp, color = NeonGray),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                msg.text,
                                style = TextStyle(fontSize = 13.sp, color = NeonWhite, lineHeight = 18.sp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Helpers ---

private fun parseTranscript(json: String): List<TranscriptEntry> {
    return try {
        val type = object : TypeToken<List<TranscriptEntry>>() {}.type
        Gson().fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}分${s.toString().padStart(2, '0')}秒"
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = diff / 3600000
    val days = diff / 86400000

    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "$minutes 分钟前"
        hours < 24 -> "$hours 小时前"
        days < 7 -> "$days 天前"
        else -> java.text.SimpleDateFormat("MM-dd", java.util.Locale.CHINA).format(java.util.Date(timestamp))
    }
}
