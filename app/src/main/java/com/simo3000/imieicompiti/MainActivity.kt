package com.simo3000.imieicompiti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.simo3000.imieicompiti.ui.navigation.NavGraph
import com.simo3000.imieicompiti.ui.theme.AppTheme
import com.simo3000.imieicompiti.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by themeViewModel.isDark.collectAsState()
            AppTheme(darkTheme = isDark) {
                NavGraph()
            }
        }
    }
}