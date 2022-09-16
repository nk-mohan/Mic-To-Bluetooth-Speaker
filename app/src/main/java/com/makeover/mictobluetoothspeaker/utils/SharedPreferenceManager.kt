package com.makeover.mictobluetoothspeaker.utils

import android.content.Context
import android.content.SharedPreferences
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import javax.inject.Inject

class SharedPreferenceManager @Inject constructor(){

    companion object {
        private const val MODE = Context.MODE_PRIVATE
        private lateinit var preferences: SharedPreferences

        fun init(context: Context) {
            preferences = context.getSharedPreferences(AppConstants.SHARED_PREFERENCE_STORAGE_NAME, MODE)
        }

        /**
         * SharedPreferences extension function, so we won't need to call edit() and apply()
         * ourselves on every SharedPreferences operation.
         */
        private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
            val editor = edit()
            operation(editor)
            editor.apply()
        }

        fun setStringValue(key: String, value: String) {
            preferences.edit {
                it.putString(key, value)
            }
        }

        fun getStringValue(key: String): String? {
            return preferences.getString(key, null)
        }

        fun setLongValue(key: String, value: Long) {
            preferences.edit {
                it.putLong(key, value)
            }
        }

        fun getLongValue(key: String): Long {
            return preferences.getLong(key, 0)
        }

        fun setStringSet(key: String, value: Set<String>) {
            preferences.edit {
                it.putStringSet(key, value)
            }
        }

        fun getStringSet(key: String): Set<String>? {
            return preferences.getStringSet(key,null)
        }

        fun setBooleanValue(key: String, value: Boolean) {
            preferences.edit {
                it.putBoolean(key, value)
            }
        }

        fun getBooleanValue(key: String): Boolean {
            return preferences.getBoolean(key, false)
        }

        fun setIntValue(key: String, value: Int) {
            preferences.edit {
                it.putInt(key, value)
            }
        }

        fun getIntValue(key: String, value: Int = 0): Int {
            return preferences.getInt(key, value)
        }

        fun clearKeyValue(key: String) {
            preferences.edit {
                it.remove(key)
            }
        }
    }

}