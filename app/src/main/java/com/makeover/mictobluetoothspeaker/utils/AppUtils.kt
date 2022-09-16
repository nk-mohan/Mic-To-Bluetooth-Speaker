package com.makeover.mictobluetoothspeaker.utils

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants

object AppUtils {
    fun getNightMode(): Int {
        return when (SharedPreferenceManager.getIntValue(AppConstants.DAY_NIGHT_MODE, -1)) {
            AppConstants.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppConstants.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }
        return displayMetrics.widthPixels
    }
}