package com.example.googoose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.googoose.ui.main.GooGooseApp
import com.example.googoose.ui.theme.GooGooseTheme
import com.example.googoose.viewmodel.GooGooseViewModel
import com.example.googoose.viewmodel.GooGooseViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = GooGooseViewModelFactory((application as GooGooseApplication).repository)
        setContent {
            val viewModel: GooGooseViewModel = viewModel(factory = factory)
            val state by viewModel.state.collectAsState()
            GooGooseTheme(textSizePreset = state.textSize) {
                GooGooseApp(viewModel = viewModel)
            }
        }
    }
}
