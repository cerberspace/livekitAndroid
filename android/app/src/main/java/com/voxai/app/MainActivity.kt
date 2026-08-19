package com.voxai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.voxai.app.ui.navigation.VoxAINavGraph
import com.voxai.app.ui.theme.NeonBlack
import com.voxai.app.ui.theme.VoxAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            VoxAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NeonBlack,
                ) {
                    VoxAINavGraph()
                }
            }
        }
    }
}
