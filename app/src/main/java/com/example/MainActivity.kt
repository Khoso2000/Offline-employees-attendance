package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.StaffTrackDatabase
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.StaffTrackRepository
import com.example.ui.StaffTrackApp
import com.example.ui.state.StaffTrackViewModel
import com.example.ui.theme.StaffTrackTheme
import com.example.util.NetworkMonitor

class MainActivity : ComponentActivity() {

    private val viewModel: StaffTrackViewModel by viewModels {
        val db = StaffTrackDatabase.getInstance(applicationContext)
        val settingsRepo = SettingsRepository(applicationContext)
        val repository = StaffTrackRepository(db, settingsRepo)
        val networkMonitor = NetworkMonitor(applicationContext)
        StaffTrackViewModel.Factory(repository, networkMonitor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            StaffTrackTheme(themeMode = uiState.settings.themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    StaffTrackApp(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}
