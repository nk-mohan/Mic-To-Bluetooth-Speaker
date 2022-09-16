package com.makeover.mictobluetoothspeaker.ui.micrecording

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.makeover.mictobluetoothspeaker.services.MicToSpeakerService

class MicRecordingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    var service = MutableLiveData<MicToSpeakerService?>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
           val serviceBinder = binder as MicToSpeakerService.ServiceBinder
            service.postValue(serviceBinder.service)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
           service.postValue(null)
        }
    }

    fun getServiceConnection(): ServiceConnection {
        return serviceConnection
    }
}