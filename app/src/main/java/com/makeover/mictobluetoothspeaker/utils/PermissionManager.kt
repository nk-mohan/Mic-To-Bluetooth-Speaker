package com.makeover.mictobluetoothspeaker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants

object PermissionManager {

    fun requestAudioRecordingPermission(
        activity: Activity,
        permissionAlertDialog: PermissionAlertDialog,
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        permissionDialogListener: PermissionDialogListener
    ) {
        val hasRecordAudioPermission = isPermissionAllowed(activity, Manifest.permission.RECORD_AUDIO)
        val hasBluetoothPermission = hasBluetoothPermission(activity)

        val permissionsToRequest = mutableListOf<String>()
        if (!hasRecordAudioPermission) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (!hasBluetoothPermission) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (permissionsToRequest.isNotEmpty()) {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT) -> {
                    showPermissionPopUpForAudioRecord(permissionsLauncher, permissionsToRequest, permissionAlertDialog, permissionDialogListener)
                }
                SharedPreferenceManager.getBooleanValue(AppConstants.AUDIO_RECORD_PERMISSION_ASKED) -> {
                    permissionAlertDialog.showPermissionInstructionDialog(PermissionAlertDialog.getDeniedMicPermissionType(),
                        object : PermissionDialogListener {
                            override fun onPositiveButtonClicked() {
                                openSettingsForPermission(activity)
                                permissionDialogListener.onPositiveButtonClicked()
                            }

                            override fun onNegativeButtonClicked() {
                                permissionDialogListener.onNegativeButtonClicked()
                            }
                        })
                }
                else -> {
                    showPermissionPopUpForAudioRecord(permissionsLauncher, permissionsToRequest, permissionAlertDialog, permissionDialogListener)
                }
            }
        }
    }

    private fun hasBluetoothPermission(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || isPermissionAllowed(activity, Manifest.permission.BLUETOOTH_CONNECT)
    }

    private fun showPermissionPopUpForAudioRecord(
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        permissionsToRequest: MutableList<String>,
        permissionAlertDialog: PermissionAlertDialog,
        permissionDialogListener: PermissionDialogListener
    ) {
        permissionAlertDialog.showPermissionInstructionDialog(
            PermissionAlertDialog.getMicPermissionType(),
            object : PermissionDialogListener {
                override fun onPositiveButtonClicked() {
                    SharedPreferenceManager.setBooleanValue(AppConstants.AUDIO_RECORD_PERMISSION_ASKED, true)
                    SharedPreferenceManager.setBooleanValue(AppConstants.BLUETOOTH_PERMISSION_ASKED, true)
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    permissionDialogListener.onPositiveButtonClicked()
                }

                override fun onNegativeButtonClicked() {
                    permissionDialogListener.onNegativeButtonClicked()
                }
            })

    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun requestBluetoothPermission(
        activity: Activity,
        permissionAlertDialog: PermissionAlertDialog,
        permissionsLauncher: ActivityResultLauncher<Array<String>>
    ) {
        val hasBluetoothPermission = isPermissionAllowed(activity, Manifest.permission.BLUETOOTH_CONNECT)

        val permissionsToRequest = mutableListOf<String>()
        if (!hasBluetoothPermission) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (permissionsToRequest.isNotEmpty()) {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT) -> {
                    showPermissionPopUpForBluetooth(permissionsLauncher, permissionsToRequest, permissionAlertDialog)
                }
                SharedPreferenceManager.getBooleanValue(AppConstants.BLUETOOTH_PERMISSION_ASKED) -> {
                    permissionAlertDialog.showPermissionInstructionDialog(PermissionAlertDialog.BLUETOOTH_PERMISSION_DENIED,
                        object : PermissionDialogListener {
                            override fun onPositiveButtonClicked() {
                                openSettingsForPermission(activity)
                            }

                            override fun onNegativeButtonClicked() {
                                //Not Needed
                            }
                        })
                }
                else -> {
                    showPermissionPopUpForBluetooth(permissionsLauncher, permissionsToRequest, permissionAlertDialog)
                }
            }
        }
    }

    private fun showPermissionPopUpForBluetooth(
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        permissionsToRequest: MutableList<String>,
        permissionAlertDialog: PermissionAlertDialog
    ) {
        permissionAlertDialog.showPermissionInstructionDialog(
            PermissionAlertDialog.BLUETOOTH_PERMISSION,
            object : PermissionDialogListener {
                override fun onPositiveButtonClicked() {
                    SharedPreferenceManager.setBooleanValue(AppConstants.BLUETOOTH_PERMISSION_ASKED, true)
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                }

                override fun onNegativeButtonClicked() {
                    //Not Needed
                }
            })
    }

    fun requestStoragePermission(
        activity: Activity,
        permissionAlertDialog: PermissionAlertDialog,
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        permissionDialogListener: PermissionDialogListener
    ) {
        val hasReadPermission = isPermissionAllowed(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val hasWritePermission = isPermissionAllowed(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val minSdk29 = Build.VERSION.SDK_INT > Build.VERSION_CODES.P

        val writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!hasReadPermission) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionPopUpForStorage(
                        permissionsLauncher,
                        permissionsToRequest,
                        permissionAlertDialog,
                        permissionDialogListener
                    )
                }
                SharedPreferenceManager.getBooleanValue(AppConstants.STORAGE_PERMISSION_ASKED) -> {
                    permissionAlertDialog.showPermissionInstructionDialog(PermissionAlertDialog.STORAGE_PERMISSION_DENIED,
                        object : PermissionDialogListener {
                            override fun onPositiveButtonClicked() {
                                openSettingsForPermission(activity)
                            }

                            override fun onNegativeButtonClicked() {
                                permissionDialogListener.onNegativeButtonClicked()
                            }
                        })
                }
                else -> {
                    showPermissionPopUpForStorage(permissionsLauncher, permissionsToRequest, permissionAlertDialog, permissionDialogListener)
                }
            }
        }
    }

    private fun showPermissionPopUpForStorage(
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
        permissionsToRequest: MutableList<String>,
        permissionAlertDialog: PermissionAlertDialog,
        permissionDialogListener: PermissionDialogListener
    ) {
        permissionAlertDialog.showPermissionInstructionDialog(PermissionAlertDialog.STORAGE_PERMISSION,
            object : PermissionDialogListener {
                override fun onPositiveButtonClicked() {
                    SharedPreferenceManager.setBooleanValue(AppConstants.STORAGE_PERMISSION_ASKED, true)
                    permissionsLauncher.launch(permissionsToRequest.toTypedArray())
                }

                override fun onNegativeButtonClicked() {
                    permissionDialogListener.onNegativeButtonClicked()
                }
            })
    }

    private fun openSettingsForPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivity(intent)
    }

    /**
     * Calling this method to check the permission status
     *
     * @param context    Context of the activity
     * @param permission Permission to ask
     * @return boolean True if grand the permission
     */
    fun isPermissionAllowed(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isReadFilePermissionAllowed(context: Context): Boolean {
        return isPermissionAllowed(context, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun isWriteFilePermissionAllowed(context: Context): Boolean {
        val minSdk29 = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
        return minSdk29 || isPermissionAllowed(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}