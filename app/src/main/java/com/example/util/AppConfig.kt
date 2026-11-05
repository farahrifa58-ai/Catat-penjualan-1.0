package com.example.util

import android.content.Context
import android.content.SharedPreferences

object AppConfig {
    /**
     * Set to true for Trial Version (30-40 min limit), false for Full Version.
     * Can be switched when generating CatatJualan_Trial.apk vs CatatJualan_Full.apk.
     */
    var IS_TRIAL: Boolean = false

    const val DEVELOPER_EMAIL: String = "rendykurniawan0311@gmail.com"
    const val TRIAL_LIMIT_MINUTES: Long = 30 // Exactly 30 minutes as requested

    private const val PREFS_NAME = "catat_jualan_trial_prefs"
    private const val KEY_FIRST_LAUNCH = "trial_first_launch_timestamp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun initTrialTracking(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis()).apply()
        }
    }

    fun getElapsedMinutes(context: Context): Long {
        val prefs = getPrefs(context)
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        if (firstLaunch == 0L) return 0L
        val diffMs = System.currentTimeMillis() - firstLaunch
        return if (diffMs > 0) diffMs / (60 * 1000) else 0L
    }

    fun isTrialExpired(context: Context): Boolean {
        if (!IS_TRIAL) return false
        return getElapsedMinutes(context) >= TRIAL_LIMIT_MINUTES
    }

    fun isTrialWarning(context: Context): Boolean {
        if (!IS_TRIAL) return false
        val elapsed = getElapsedMinutes(context)
        return elapsed in (TRIAL_LIMIT_MINUTES - 5) until TRIAL_LIMIT_MINUTES
    }
}