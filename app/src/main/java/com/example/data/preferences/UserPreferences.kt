package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT,
    DARK
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("catat_jualan_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val code = prefs.getString("selected_language", AppLanguage.INDONESIAN.code)
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.INDONESIAN
    }

    private fun loadThemeMode(): AppThemeMode {
        val mode = prefs.getString("selected_theme", AppThemeMode.LIGHT.name)
        return try {
            AppThemeMode.valueOf(mode ?: AppThemeMode.LIGHT.name)
        } catch (_: Exception) {
            AppThemeMode.LIGHT
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("selected_language", language.code).apply()
        _language.value = language
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("selected_theme", mode.name).apply()
        _themeMode.value = mode
    }
}
