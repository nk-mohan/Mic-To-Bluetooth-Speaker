package com.makeover.mictobluetoothspeaker.utils

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.databinding.PermissionInstructionDialogBinding

class PermissionAlertDialog constructor(private var activity: Activity) {

    fun showPermissionInstructionDialog(
        permissionType: String,
        permissionDialogListener: PermissionDialogListener
    ) {
        val dialogBuilder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
        val inflater: LayoutInflater = activity.layoutInflater
        val dialogBinding = PermissionInstructionDialogBinding.inflate(inflater)
        dialogBinding.dialogIcon.setImageResource(getDialogIcon(permissionType))
        dialogBinding.dialogDescription.text = getDialogDescription(permissionType)
        dialogBuilder.apply {
            setCancelable(false)
            setView(dialogBinding.root)
            setPositiveButton(activity.getString(R.string.continue_label)) { dialog, _ ->
                dialog.dismiss()
                permissionDialogListener.onPositiveButtonClicked()
            }
            setNegativeButton(activity.getString(R.string.not_now_label)) { dialog, _ ->
                dialog.dismiss()
                permissionDialogListener.onNegativeButtonClicked()
            }
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        adjustAlertDialogWidth(activity, alertDialog)
    }

    private fun getDialogDescription(permissionType: String): CharSequence {
        return when (permissionType) {
            MIC_PERMISSION -> activity.getString(R.string.mic_permission_alert_label)
            MIC_PERMISSION_DENIED -> activity.getString(R.string.mic_permission_denied_alert_label)
            MIC_BLUETOOTH_PERMISSION -> activity.getString(R.string.mic_bluetooth_alert_label)
            MIC_BLUETOOTH_PERMISSION_DENIED -> activity.getString(R.string.mic_bluetooth_denied_alert_label)
            BLUETOOTH_PERMISSION -> activity.getString(R.string.bluetooth_alert_label)
            BLUETOOTH_PERMISSION_DENIED -> activity.getString(R.string.bluetooth_denied_alert_label)
            else -> activity.getString(R.string.mic_permission_alert_label)
        }
    }

    private fun getDialogIcon(permissionType: String): Int {
        return when(permissionType) {
            BLUETOOTH_PERMISSION, BLUETOOTH_PERMISSION_DENIED -> R.drawable.ic_bluetooth_permission
            else -> R.drawable.ic_record_permission
        }
    }

    private fun adjustAlertDialogWidth(activity: Activity, alertDialog: AlertDialog) {
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertDialog.window!!.attributes)
        layoutParams.width = (AppUtils.getScreenWidth(activity) * 0.78).toInt()
        alertDialog.window!!.attributes = layoutParams
    }

    companion object {
        fun getDeniedMicPermissionType(): String {
           return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                MIC_PERMISSION_DENIED
            else MIC_BLUETOOTH_PERMISSION_DENIED
        }

        fun getMicPermissionType(): String {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                MIC_PERMISSION
            else MIC_BLUETOOTH_PERMISSION
        }

        const val MIC_PERMISSION = "mic_permission"
        const val MIC_PERMISSION_DENIED = "mic_permission_denied"
        const val MIC_BLUETOOTH_PERMISSION = "mic_bluetooth_permission"
        const val MIC_BLUETOOTH_PERMISSION_DENIED = "mic_bluetooth_permission_denied"
        const val BLUETOOTH_PERMISSION = "bluetooth_permission"
        const val BLUETOOTH_PERMISSION_DENIED = "bluetooth_permission_denied"
    }
}