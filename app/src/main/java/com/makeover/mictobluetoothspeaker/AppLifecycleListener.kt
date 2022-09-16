package com.makeover.mictobluetoothspeaker

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleListener : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        isForeground = false
        // app moved to background
        Log.d(TAG, "App moved to background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun appLifeCycleOnCreate() {
        Log.d(TAG, "OnCreate")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        isForeground = true
        // app moved to foreground
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResumeCallback() {
        Log.d(TAG, "App OnResume $isForeground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed() {
        Log.d(TAG, "app destroyed")
    }

    companion object {
        var isForeground = false
        var isRecording = false
    }

}