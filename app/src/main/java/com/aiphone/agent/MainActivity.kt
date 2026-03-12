package com.aiphone.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiphone.agent.data.local.preferences.SecurePreferences
import com.aiphone.agent.presentation.navigation.AppNavigation
import com.aiphone.agent.presentation.theme.AIPhoneAgentTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var securePreferences: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AIPhoneAgentTheme {
                AppNavigation(
                    isOnboardingComplete = securePreferences.isOnboardingComplete()
                )
            }
        }
    }
}
