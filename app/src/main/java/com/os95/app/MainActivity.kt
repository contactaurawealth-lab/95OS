package com.os95.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.os95.app.core.datastore.OS95Preferences
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.navigation.OS95App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as OS95Application).container

        setContent {
            val preferences by appContainer.preferencesManager.preferencesFlow.collectAsState(
                initial = OS95Preferences()
            )

            OS95Theme(themeMode = preferences.themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OS95Theme.colors.background)
                ) {
                    OS95App(
                        container = appContainer,
                        isOnboardingCompleted = preferences.isOnboardingCompleted
                    )
                }
            }
        }
    }
}
