package com.makeover.mictobluetoothspeaker.ui.views

import android.app.Activity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.databinding.PlayAudioDialogBinding
import com.makeover.mictobluetoothspeaker.ui.recordings.AudioFileData
import com.makeover.mictobluetoothspeaker.utils.AppUtils
import javax.inject.Inject

class AudioAlertDialog @Inject constructor(private var activity: Activity) {

    private var mediaController: MediaController = MediaController(activity)
    private var alertDialog:AlertDialog? = null


    fun playAudioFile(audioFileData: AudioFileData) {
        val dialogBuilder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
        val inflater: LayoutInflater = activity.layoutInflater
        val dialogBinding = PlayAudioDialogBinding.inflate(inflater)
        dialogBinding.textAudioDuration.text = audioFileData.fileDuration
        dialogBinding.textAudioFileName.text = audioFileData.fileName

        dialogBinding.imageAudioAction.setOnClickListener {
            playAudio(dialogBinding, audioFileData)
        }

        mediaController.checkStateOfPlayer(
            dialogBinding.imageAudioAction,
            dialogBinding.seekAudioProgress,
            dialogBinding.textAudioDuration
        )

        playAudio(dialogBinding, audioFileData)

        dialogBuilder.apply {
            setCancelable(false)
            setView(dialogBinding.root)
        }

        alertDialog = dialogBuilder.create()
        alertDialog?.show()

        dialogBinding.cancelButton.setOnClickListener {
            closeAlertDialog()
         }

        adjustAlertDialogWidth(activity, alertDialog)
    }

    fun closeAlertDialog() {
        if (alertDialog?.isShowing == true) {
            mediaController.resetAudioPlayer()
            alertDialog?.dismiss()
        }
    }

    private fun playAudio(dialogBinding: PlayAudioDialogBinding, audioFileData: AudioFileData) {
        with(mediaController) {
            setMediaResource(
                audioFileData.pathName,
                audioFileData.fileDuration,
                dialogBinding.imageAudioAction
            )
            setMediaSeekBar(dialogBinding.seekAudioProgress)
            setMediaTimer(dialogBinding.textAudioDuration)
            handlePlayer()
        }
    }

    private fun adjustAlertDialogWidth(activity: Activity, alertDialog: AlertDialog?) {
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertDialog?.window!!.attributes)
        layoutParams.width = (AppUtils.getScreenWidth(activity) * 0.90).toInt()
        alertDialog.window!!.attributes = layoutParams
    }

}