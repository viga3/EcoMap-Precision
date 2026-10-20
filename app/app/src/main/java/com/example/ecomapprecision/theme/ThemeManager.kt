package com.example.ecomapprecision.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

object ThemeManager {
    private const val PREFS_NAME = "ecomap_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val savedModeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val mode = try {
            AppThemeMode.valueOf(savedModeStr ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
        _themeMode.value = mode
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        _themeMode.value = mode
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
