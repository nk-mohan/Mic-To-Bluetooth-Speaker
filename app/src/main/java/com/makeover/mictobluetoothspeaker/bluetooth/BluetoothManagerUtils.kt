package com.makeover.mictobluetoothspeaker.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context


object BluetoothManagerUtils {

    fun getBlueToothAdapter(context : Context) : BluetoothAdapter? {
        val bluetoothService = context.getSystemService(Context.BLUETOOTH_SERVICE)
        return if (bluetoothService == null){
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter
        } else {
            val bluetoothManager = bluetoothService as android.bluetooth.BluetoothManager
            bluetoothManager.adapter
        }
    }

    /**
     * Converts BluetoothAdapter states into local string representations.
     *
     * @param state [BluetoothAdapter] states
     * @return the bluetooth state string
     */
    fun stateToString(state: Int): String {
        return when (state) {
            BluetoothAdapter.STATE_DISCONNECTED -> "DISCONNECTED"
            BluetoothAdapter.STATE_CONNECTED -> "CONNECTED"
            BluetoothAdapter.STATE_CONNECTING -> "CONNECTING"
            BluetoothAdapter.STATE_DISCONNECTING -> "DISCONNECTING"
            BluetoothAdapter.STATE_OFF -> "OFF"
            BluetoothAdapter.STATE_ON -> "ON"
            BluetoothAdapter.STATE_TURNING_OFF ->                 // Indicates the local Bluetooth adapter is turning off. Local clients should immediately
                // attempt graceful disconnection of any remote links.
                "TURNING_OFF"
            BluetoothAdapter.STATE_TURNING_ON ->                 // Indicates the local Bluetooth adapter is turning on. However local clients should wait
                // for STATE_ON before attempting to use the adapter.
                "TURNING_ON"
            else -> "INVALID"
        }
    }
}