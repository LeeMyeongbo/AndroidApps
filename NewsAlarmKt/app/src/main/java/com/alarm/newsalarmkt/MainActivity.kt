package com.alarm.newsalarmkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alarm.newsalarmkt.ui.MainView
import com.alarm.newsalarmkt.ui.theme.NewsAlarmKtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAlarmKtTheme {
                MainView()
            }
        }
    }
}
