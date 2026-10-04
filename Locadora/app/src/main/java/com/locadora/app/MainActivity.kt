package com.locadora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.locadora.app.ui.LocadoraNavHost
import com.locadora.app.ui.theme.LocadoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocadoraTheme {
                LocadoraNavHost()
            }
        }
    }
}
