package com.makeover.mictobluetoothspeaker.ui.settings

import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.makeover.mictobluetoothspeaker.utils.SharedPreferenceManager
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text

    val selectedTheme = MutableLiveData<Int>()

    fun getSelectedTheme() {
        selectedTheme.postValue(SharedPreferenceManager.getIntValue(AppConstants.DAY_NIGHT_MODE))
    }

    var originalConfiguration: Configuration? = null
}