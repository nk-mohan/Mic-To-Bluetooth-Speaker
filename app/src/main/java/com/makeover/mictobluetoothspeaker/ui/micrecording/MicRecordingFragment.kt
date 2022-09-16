package com.makeover.mictobluetoothspeaker.ui.micrecording

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.makeover.mictobluetoothspeaker.AppLifecycleListener
import com.makeover.mictobluetoothspeaker.databinding.FragmentMicRecordingBinding
import com.makeover.mictobluetoothspeaker.MyApplication
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import com.makeover.mictobluetoothspeaker.services.MicToSpeakerService

class MicRecordingFragment : MicRecordingParent(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViews(inflater, container)
        setObservers()
        setListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bluetoothManager.initBlueToothListener()
    }

    private fun initViews(inflater: LayoutInflater, container: ViewGroup?) {
        micRecordingViewModel = ViewModelProvider(this)[MicRecordingViewModel::class.java]
        _binding = FragmentMicRecordingBinding.inflate(inflater, container, false)
        serviceIntent = Intent(MyApplication.getContext(), MicToSpeakerService::class.java)
        fromNotification = arguments?.getBoolean(AppConstants.FROM_NOTIFICATION, false) ?: false
    }

    private fun setListeners() {
        binding.recordIcon.setOnClickListener(this)
        binding.bluetoothStatus.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.recordIcon -> performRecording()
            binding.bluetoothStatus -> checkBluetoothStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        if (fromNotification || AppLifecycleListener.isRecording)
            bindService()
    }

    override fun onPause() {
        super.onPause()
        if (service?.recordingStartedLiveData?.value == true)
            unbindService()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothManager.destroyBlueToothListener()
        _binding = null
    }
}