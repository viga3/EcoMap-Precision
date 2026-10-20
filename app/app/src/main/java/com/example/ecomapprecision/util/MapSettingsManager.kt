package com.example.ecomapprecision.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MapSettingsManager {
    private const val PREFS_NAME = "ecomap_map_prefs"
    private const val KEY_HIDE_MAP_POIS = "key_hide_map_pois"

    private val _hideMapPois = MutableStateFlow(false)
    val hideMapPois: StateFlow<Boolean> = _hideMapPois.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        _hideMapPois.value = prefs.getBoolean(KEY_HIDE_MAP_POIS, false)
    }

    fun setHideMapPois(context: Context, hide: Boolean) {
        _hideMapPois.value = hide
        getPrefs(context).edit().putBoolean(KEY_HIDE_MAP_POIS, hide).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
