package com.makeover.mictobluetoothspeaker.utils

import android.annotation.SuppressLint
import com.makeover.mictobluetoothspeaker.MyApplication
import com.makeover.mictobluetoothspeaker.R
import java.util.concurrent.TimeUnit

object RecordingUtils {

    /**
     * This function formats the duration of recording.
     *
     * @param timeInSeconds recording duration in seconds
     * @return return the formatted duration
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun getFormattedRecordingTime(timeInSeconds: Long): String {
        return if (timeInSeconds < 1) {
            MyApplication.getContext().getString(R.string.timer_default_label)
        } else {
            String.format("%02d:%02d:%02d",
                TimeUnit.SECONDS.toHours(timeInSeconds),
                TimeUnit.SECONDS.toMinutes(timeInSeconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeInSeconds)),
                TimeUnit.SECONDS.toSeconds(timeInSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeInSeconds)))
        }
    }

    /**
     * This function formats the duration of recording.
     *
     * @param timeInSeconds recording duration in seconds
     * @return return the formatted duration
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun getFormattedRecordingTimeForNotification(timeInSeconds: Long): String {
        return if (timeInSeconds < 1) {
            "00:00"
        } else {
            /* if call duration greater than one hour change duration format */
            if (timeInSeconds >= 3600) {
                String.format("%02d:%02d:%02d",
                    TimeUnit.SECONDS.toHours(timeInSeconds),
                    TimeUnit.SECONDS.toMinutes(timeInSeconds) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeInSeconds)),
                    TimeUnit.SECONDS.toSeconds(timeInSeconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeInSeconds)))
            } else {
                String.format("%02d:%02d",
                    TimeUnit.SECONDS.toMinutes(timeInSeconds) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeInSeconds)),
                    TimeUnit.SECONDS.toSeconds(timeInSeconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeInSeconds)))
            }
        }
    }
}