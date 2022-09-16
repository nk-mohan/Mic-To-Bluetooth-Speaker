package com.makeover.mictobluetoothspeaker.ui.views

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.makeover.mictobluetoothspeaker.R
import javax.inject.Inject

class AlertDialogView @Inject constructor() {

    fun showChooseOneAlertDialog(
        activity: Activity?,
        title: String,
        choices: Array<String>,
        selectedPosition: Int,
        positiveButtonName: String,
        negativeButtonName: String,
        listener: ChooseOneAlertDialogListener
    ) {
        activity?.let {
            var updatedPosition = selectedPosition
            val dialogBuilder = AlertDialog.Builder(it, R.style.AppCompatAlertDialogStyle)
            dialogBuilder.setTitle(title)
            dialogBuilder.setSingleChoiceItems(choices, selectedPosition) { _, selectedItem ->
                updatedPosition = selectedItem
            }
            dialogBuilder.setPositiveButton(positiveButtonName) { dialog, _ ->
                listener.selectedItem(updatedPosition)
                dialog.dismiss()
            }
            dialogBuilder.setNegativeButton(negativeButtonName) { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }

    fun showConfirmationDialog(
        activity: Activity?,
        title: String,
        description: String,
        positiveButtonName: String,
        negativeButtonName: String,
        listener: ConfirmationAlertDialogListener
    ) {
        activity?.let {
            val dialogBuilder = AlertDialog.Builder(it, R.style.AppCompatAlertDialogStyle)
            dialogBuilder.setTitle(title)
            dialogBuilder.setMessage(description)
            dialogBuilder.setNegativeButton(negativeButtonName) { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton(positiveButtonName) { dialog, _ ->
                dialog.dismiss()
                listener.onConfirmation(true)
            }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }
}