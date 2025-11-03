package com.chain.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chain.app.presentation.theme.ChainTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Chain messaging platform.
 * Hosts the Compose UI and handles navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChainApp()
                }
            }
        }
    }
}

@Composable
fun ChainApp() {
    // Placeholder for now - will be replaced with navigation
    Text("Welcome to Chain - Decentralized Messaging Platform")
}

@Preview(showBackground = true)
@Composable
fun ChainAppPreview() {
    ChainTheme {
        ChainApp()
    }
}
