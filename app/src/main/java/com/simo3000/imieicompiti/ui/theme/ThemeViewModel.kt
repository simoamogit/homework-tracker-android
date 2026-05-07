package com.simo3000.imieicompiti.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _isDark = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    fun toggle() {
        val new = !_isDark.value
        _isDark.value = new
        prefs.edit().putBoolean("dark_mode", new).apply()
    }
}