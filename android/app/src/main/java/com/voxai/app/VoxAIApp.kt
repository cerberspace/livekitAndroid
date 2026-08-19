package com.voxai.app

import android.app.Application
import io.livekit.android.util.LoggingLevel
import io.livekit.android.LiveKit

class VoxAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure LiveKit SDK logging
        LiveKit.loggingLevel = LoggingLevel.WARN
    }
}
