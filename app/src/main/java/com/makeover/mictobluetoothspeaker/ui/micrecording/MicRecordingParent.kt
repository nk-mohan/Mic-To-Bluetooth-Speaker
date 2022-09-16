package com.makeover.mictobluetoothspeaker.ui.micrecording

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.TAG
import com.makeover.mictobluetoothspeaker.bluetooth.BluetoothListener
import com.makeover.mictobluetoothspeaker.bluetooth.RecordingBluetoothManager
import com.makeover.mictobluetoothspeaker.databinding.FragmentMicRecordingBinding
import com.makeover.mictobluetoothspeaker.services.MicToSpeakerService
import com.makeover.mictobluetoothspeaker.utils.PermissionAlertDialog
import com.makeover.mictobluetoothspeaker.utils.PermissionDialogListener
import com.makeover.mictobluetoothspeaker.utils.PermissionManager
import com.makeover.mictobluetoothspeaker.utils.RecordingUtils
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants

open class MicRecordingParent : Fragment(), BluetoothListener {

    private val permissionAlertDialog: PermissionAlertDialog by lazy {
        PermissionAlertDialog(requireActivity())
    }

    private val permissionDialogListener = object : PermissionDialogListener {
        override fun onPositiveButtonClicked() {
            //Not Needed
        }

        override fun onNegativeButtonClicked() {
            permissionNotDenied = true
        }

    }
    protected lateinit var micRecordingViewModel: MicRecordingViewModel
    protected var _binding: FragmentMicRecordingBinding? = null
    protected val binding get() = _binding!!

    private var permissionNotDenied = true
    protected var fromNotification = false
    protected lateinit var serviceIntent: Intent
    protected var service : MicToSpeakerService? = null

    protected val bluetoothManager : RecordingBluetoothManager by lazy { RecordingBluetoothManager(requireContext(), this) }

    protected fun setObservers() {

        micRecordingViewModel.service.observe(viewLifecycleOwner) {
            service = it
            updateTimer()
            updateRecordingStatus()
        }
    }

    private fun updateTimer() {
        Log.e(TAG, "${AppConstants.APP_NAME} updateTimer called")
        service?.recordingTimeLiveData?.observe(viewLifecycleOwner) { time ->
            Log.e(TAG, "${AppConstants.APP_NAME} received timer : $time")
            binding.recordingTime.text = RecordingUtils.getFormattedRecordingTime(time)
        }
    }

    private fun updateRecordingStatus() {
        if (service == null) {
            binding.recordIcon.text = getString(R.string.start_record)
            hideAudioWaves()
        } else {
            service?.recordingStartedLiveData?.observe(viewLifecycleOwner) { recordingStarted ->
                if (recordingStarted) {
                    binding.recordIcon.text = getString(R.string.end_record)
                    showAudioWaves()
                } else {
                    binding.recordIcon.text = getString(R.string.start_record)
                    hideAudioWaves()
                }
            }
            service?.recordingEndedLiveData?.observe(viewLifecycleOwner) { recordingEnded ->
                if (recordingEnded) {
                    unbindService()
                    requireActivity().stopService(serviceIntent)
                }
            }
        }
    }

    private fun hideAudioWaves() {
        binding.leftAudioWave.visibility = View.GONE
        binding.rightAudioWave.visibility = View.GONE
    }

    private fun showAudioWaves() {
        binding.leftAudioWave.visibility = View.VISIBLE
        binding.rightAudioWave.visibility = View.VISIBLE
        Glide.with(this).load(R.drawable.ic_left_audio_wave).into(binding.leftAudioWave)
        Glide.with(this).load(R.drawable.ic_right_audio_wave).into(binding.rightAudioWave)
    }

    private fun checkAndUpdateBlueToothState() {
        if (bluetoothManager.isBluetoothAvailable())
            _binding?.bluetoothStatus?.text = bluetoothManager.getConnectedDeviceName()
        else
            _binding?.bluetoothStatus?.text = getString(R.string.bluetooth_turned_off)
    }

    protected fun checkBluetoothStatus() {
        if (bluetoothManager.isBluetoothAvailable()) {
            if (isBluetoothPermissionGranted && bluetoothManager.isBluetoothDeviceNotConnected()) {
                val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivity(settingsIntent)
            }
        } else {
            if (isBluetoothPermissionGranted)
                bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    protected fun performRecording() {
        if (service?.recordingStartedLiveData?.value == true) {
            updateRecordingStatus()
            unbindService()
            requireActivity().stopService(serviceIntent)
        } else {
            if (isRecordPermissionGranted) {
                updateRecordingStatus()
                requireActivity().startService(serviceIntent)
                bindService()
            }
        }
    }

    protected fun bindService() {
        requireActivity().bindService(serviceIntent, micRecordingViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE)
    }

    private val audioRecordPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioRecordPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: PermissionManager.isPermissionAllowed(requireContext(), Manifest.permission.RECORD_AUDIO)

        if (audioRecordPermissionGranted)
            performRecording()

        val bluetoothPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: PermissionManager.isPermissionAllowed(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
        if (bluetoothPermissionGranted) {
            checkAndUpdateBlueToothState()
        }
    }

    /**
     * Method used to check the run time audio record permission
     *
     * @return Permission status
     */
    private val isRecordPermissionGranted: Boolean
        get() =// Verify the SDK version for permission validation
            if (PermissionManager.isPermissionAllowed(requireContext(), Manifest.permission.RECORD_AUDIO))
                true
            else {
                if (permissionNotDenied)
                    PermissionManager.requestAudioRecordingPermission(requireActivity(), permissionAlertDialog, audioRecordPermissionLauncher, permissionDialogListener)
                permissionNotDenied = false
                false
            }

    private val bluetoothEnableLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val bluetoothPermissionGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: PermissionManager.isPermissionAllowed(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)

        if (bluetoothPermissionGranted) {
            checkAndUpdateBlueToothState()
            checkBluetoothStatus()
        }
    }

    /**
     * Method used to check the run time audio record permission
     *
     * @return Permission status
     */
    private val isBluetoothPermissionGranted: Boolean
        get() =// Verify the SDK version for permission validation
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || PermissionManager.isPermissionAllowed(context, Manifest.permission.BLUETOOTH_CONNECT))
                true
            else {
                PermissionManager.requestBluetoothPermission(requireActivity(), permissionAlertDialog, bluetoothPermissionLauncher)
                false
            }

    protected fun unbindService() {
        requireActivity().unbindService(micRecordingViewModel.getServiceConnection())
    }

    override fun onBluetoothDeviceStateUpdated() {
        checkAndUpdateBlueToothState()
    }
}