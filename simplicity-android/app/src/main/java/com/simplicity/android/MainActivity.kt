package com.simplicity.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplicity.android.ui.theme.SimplicityTheme
import com.simplicity.android.ui.screens.MainScreen
import com.simplicity.android.data.model.Provider
import com.simplicity.android.data.repository.ModelRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = ModelRepository()
        
        setContent {
            SimplicityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(repository = repository)
                }
            }
        }
    }
}
