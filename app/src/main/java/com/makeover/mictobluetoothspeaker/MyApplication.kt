package com.makeover.mictobluetoothspeaker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.makeover.mictobluetoothspeaker.utils.AppUtils
import com.makeover.mictobluetoothspeaker.utils.PermissionManager
import com.makeover.mictobluetoothspeaker.utils.SharedPreferenceManager
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //register observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
        SharedPreferenceManager.init(this)
        AppCompatDelegate.setDefaultNightMode(AppUtils.getNightMode())
        if (!SharedPreferenceManager.getBooleanValue(AppConstants.SAVE_RECORDING_UPDATED)) {
            SharedPreferenceManager.setBooleanValue(AppConstants.SAVE_RECORDING_UPDATED, true)
            SharedPreferenceManager.setBooleanValue(AppConstants.SAVE_RECORDING, PermissionManager.isWriteFilePermissionAllowed(applicationContext))
        }
    }
    init {
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}