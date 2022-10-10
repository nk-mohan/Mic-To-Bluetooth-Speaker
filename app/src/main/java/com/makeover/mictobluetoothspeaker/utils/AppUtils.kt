package com.makeover.mictobluetoothspeaker.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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

    private fun getExternalStorage(applicationContext: Context): File {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) Environment.getExternalStorageDirectory() else applicationContext.filesDir
        } else applicationContext.externalMediaDirs[0]
    }

    /**
     * Returns the directory inside which a new file will be created.
     *
     * @param context    The parent context.
     * @param folderName The name of the folder in which the recorded audio will be written.
     * @return The parent pathname string.
     */
    fun getPath(context: Context, folderName: String): String {
        return getExternalStorage(context).absolutePath + File.separator + context.getString(R.string.app_name) + File.separator + folderName
    }


    /**
     * Creates folder if not exist
     *
     * @param path Path of the folder
     */
    fun createFolderIfNotExist(path: String) : File {
        val folder = File(path)
        if (!folder.exists())
            folder.mkdirs()
        return folder
    }

    fun getRecordingFile(context: Context) : File {
        val parentPath = getPath(context, context.getString(R.string.recording_folder_label))
        val parentFolder = createFolderIfNotExist(parentPath)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault()).format(Date())
        val path = "Recording_$timeStamp.aac"
        val file = File("$parentFolder/$path")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }
}