package com.makeover.mictobluetoothspeaker.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
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
    fun createFolderIfNotExist(path: String): File {
        val folder = File(path)
        if (!folder.exists())
            folder.mkdirs()
        return folder
    }

    fun getRecordingFile(context: Context): File {
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

    fun getAudioMediaUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    fun getStringSizeLengthFile(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return df.format((size / sizeKb).toDouble()) + " KB"
        else if (size < sizeGb) return df.format((size / sizeMb).toDouble()) + " MB"
        else if (size < sizeTerra) return df.format((size / sizeGb).toDouble()) + " GB"
        return ""
    }

    fun getAudioDuration(duration: Long): String {
        val df = DecimalFormat("00")
        val durationInSec = duration / 1000
        val perMinute = 60
        val perHour = perMinute * perMinute
        val totalHours = durationInSec / perHour
        val remainingMinutes = (durationInSec % perHour) / perMinute
        val remainingSec = ((durationInSec % perHour) % perMinute)
        return if (totalHours > 1) "${df.format(totalHours)}.${df.format(remainingMinutes)}.${df.format(remainingSec)}"
        else if (remainingMinutes > 1) "${df.format(remainingMinutes)}.${df.format(remainingSec)}"
        else "00.${df.format(remainingSec)}"
    }

    /**
     * Gets the formatted time.
     *
     * @param timeConsume Timestamp
     * @return String The formatted time
     */
    fun getFormattedTime(timeConsume: Int): String {
        return if (timeConsume < 60) {
            if (timeConsume < 10) "00:0$timeConsume" else "00:$timeConsume"
        } else {
            val sec = timeConsume % 60
            val min = timeConsume / 60
            if (min < 10 && sec < 10) "0$min:0$sec" else if (min < 10 && sec >= 10) "0$min:$sec" else if (min >= 10 && sec < 10) "$min:0$sec" else "$min:$sec"
        }
    }
}