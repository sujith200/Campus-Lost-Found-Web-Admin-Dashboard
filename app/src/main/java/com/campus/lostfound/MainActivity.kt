package com.campus.lostfound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.campus.lostfound.ui.AppNav
import com.campus.lostfound.ui.theme.CampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusTheme {
                Surface(color = Color(0xFFF8FAFC)) {
                    val nav = rememberNavController()
                    AppNav(nav)
                }
            }
        }
    }
}
