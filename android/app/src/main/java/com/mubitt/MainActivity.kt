package com.mubitt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.mubitt.shared.theme.MubittTheme
import com.mubitt.shared.navigation.MubittNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Mubitt App
 * 
 * Single Activity Architecture + Navigation Component
 * Standards: Uber-level UX, <200ms transitions
 * Target: San Juan market dominance over Uber/Didi
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge for modern UI
        enableEdgeToEdge()
        
        // Setup splash screen animation
        setupSplashScreenAnimation(splashScreen)
        
        setContent {
            MubittTheme {
                val navController = rememberNavController()
                MubittNavigation(navController = navController)
            }
        }
    }
    
    private fun setupSplashScreenAnimation(splashScreen: androidx.core.splashscreen.SplashScreen) {
        // TODO: Premium splash animation (Phase 1)
        // Requirements: Smooth, professional, <1s duration
    }
}

@Composable
fun MubittApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // TODO: Navigation setup (Phase 1)
        WelcomeScreen(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Mubitt - Transporte San Juan\nCompetidor premium de Uber/Didi",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MubittAppPreview() {
    MubittTheme {
        MubittApp()
    }
}