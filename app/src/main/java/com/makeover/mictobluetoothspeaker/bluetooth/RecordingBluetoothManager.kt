package com.makeover.mictobluetoothspeaker.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.TAG
import com.makeover.mictobluetoothspeaker.utils.PermissionManager
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants

class RecordingBluetoothManager(private val context: Context, private val bluetoothListener: BluetoothListener) {

    private val bluetoothAdapter : BluetoothAdapter? by lazy { BluetoothManagerUtils.getBlueToothAdapter(context) }

    private var mBluetoothHeadset: BluetoothHeadset? = null

    /**
     * Provided status of whether bluetooth turned on in the device
     * @return Boolean
     */
    fun isBluetoothAvailable() : Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    @SuppressLint("MissingPermission")
    fun getConnectedDeviceName(): CharSequence {
        return if (PermissionManager.isPermissionAllowed(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            val devices = mBluetoothHeadset?.connectedDevices
            if (devices.isNullOrEmpty()) {
                context.getString(R.string.bluetooth_not_connected)
            } else {
                // Always use first device in list. Android only supports one device.
                val bluetoothDevice = devices[0]
                return bluetoothDevice.name
            }
        } else
            context.getString(R.string.bluetooth_permission_not_available)

    }

    @SuppressLint("MissingPermission")
    fun isBluetoothDeviceNotConnected(): Boolean {
        val devices = mBluetoothHeadset?.connectedDevices
        return devices.isNullOrEmpty()
    }

    fun initBlueToothListener() {
        if (!getBluetoothProfileProxy(context, bluetoothServiceListener)) {
            Log.e(TAG, AppConstants.APP_NAME + "BluetoothAdapter.getProfileProxy(HEADSET) failed")
            return
        }

        val bluetoothHeadsetFilter = IntentFilter()
        bluetoothHeadsetFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(bluetoothHeadsetReceiver, bluetoothHeadsetFilter)
    }

    fun destroyBlueToothListener() {
        unregisterReceiver(bluetoothHeadsetReceiver)
    }

    private fun getBluetoothProfileProxy(context: Context?, listener: ServiceListener?): Boolean {
        return bluetoothAdapter?.getProfileProxy(context, listener, BluetoothProfile.HEADSET) ?: false
    }

    /**
     * Implementation of an interface that notifies BluetoothProfile IPC clients when they have been
     * connected to or disconnected from the service.
     */
    private val bluetoothServiceListener = object : ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            // Android only supports one connected Bluetooth Headset at a time.
            mBluetoothHeadset = proxy as BluetoothHeadset
            if (mBluetoothHeadset != null)
                bluetoothListener.onBluetoothDeviceStateUpdated()
        }

        /**
         * Notifies the client when the proxy object has been disconnected from the service.
         */
        override fun onServiceDisconnected(profile: Int) {
            mBluetoothHeadset = null
            bluetoothListener.onBluetoothDeviceStateUpdated()
        }
    }

    private fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?) {
        try {
            context.registerReceiver(receiver, filter)
        } catch (e : Exception){
            Log.d(TAG, AppConstants.APP_NAME + "registerReceiver Exception: $e")
        }
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver?) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e : Exception){
            Log.d(TAG, AppConstants.APP_NAME + "unregisterReceiver Exception: $e")
        }
    }

    // Intent broadcast receiver which handles changes in Bluetooth device availability.
    private val bluetoothHeadsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) {
                handleConnectionStateChange(intent)
            }
            Log.d(TAG, AppConstants.APP_NAME + "onReceive done")
        }

        private fun handleConnectionStateChange(intent: Intent) {
            val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
            Log.d(TAG, AppConstants.APP_NAME + "BluetoothHeadsetBroadcastReceiver.onReceive: "
                        + "a=ACTION_CONNECTION_STATE_CHANGED, "
                        + "s=" + BluetoothManagerUtils.stateToString(state) + ", "
                        + "sb=" + isInitialStickyBroadcast
            )
            bluetoothListener.onBluetoothDeviceStateUpdated()
        }
    }

}