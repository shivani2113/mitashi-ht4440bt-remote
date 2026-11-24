package com.mitashi.ht4440bt.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitashi.ht4440bt.remote.ui.MitashiRemoteScreen
import com.mitashi.ht4440bt.remote.ui.RemoteViewModel
import com.mitashi.ht4440bt.remote.ui.theme.MitashiRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RemoteViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            MitashiRemoteTheme(darkTheme = state.isDarkTheme) {
                MitashiRemoteScreen(viewModel = viewModel)
            }
        }
    }
}
