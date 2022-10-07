package com.makeover.mictobluetoothspeaker.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.makeover.mictobluetoothspeaker.AppLifecycleListener
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.TAG
import com.makeover.mictobluetoothspeaker.ui.MainActivity
import com.makeover.mictobluetoothspeaker.utils.AppUtils
import com.makeover.mictobluetoothspeaker.utils.PendingIntentHelper
import com.makeover.mictobluetoothspeaker.utils.RecordingUtils
import com.makeover.mictobluetoothspeaker.utils.SharedPreferenceManager
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants.Companion.APP_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

class MicToSpeakerService : Service(), CoroutineScope {

    private var isRecordingStarted = false

    private lateinit var mAudioInput: AudioRecord
    private lateinit var mAudioOutput: AudioTrack

    private val audioSource = MediaRecorder.AudioSource.MIC // for raw audio, use MediaRecorder.AudioSource.UNPROCESSED, see note in MediaRecorder section
    private val sampleRate = 44100
    private val channelInConfig = AudioFormat.CHANNEL_IN_MONO
    private val channelOutConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val mInBufferSize: Int by lazy { AudioRecord.getMinBufferSize(sampleRate, channelInConfig, audioFormat) }
    private val mOutBufferSize: Int by lazy { AudioTrack.getMinBufferSize(sampleRate, channelOutConfig, audioFormat) }

    private val handler = Handler(Looper.getMainLooper())

    //Bind Service parameter
    private val mBinder = ServiceBinder()
    val recordingTimeLiveData = MutableLiveData<Long>()
    val recordingStartedLiveData = MutableLiveData<Boolean>()
    val recordingEndedLiveData = MutableLiveData<Boolean>()

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "$APP_NAME onCreate")

        startNotification()

        mAudioInput = AudioRecord(audioSource, sampleRate, channelInConfig, audioFormat, mInBufferSize)

        mAudioOutput = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(audioFormat)
                .setChannelMask(channelOutConfig).build(),
            mOutBufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "$APP_NAME onStartCommand")
        val isRecordingEnded = intent?.getStringExtra(AppConstants.EXTRA_ACTION)
        if (isRecordingEnded.isNullOrBlank()) {
            isRecordingStarted = true
            AppLifecycleListener.isRecording = true
            recordingStartedLiveData.postValue(true)
            recordingEndedLiveData.postValue(false)
            recordingTimeLiveData.postValue(0)
            startRecordingTimer()
            launch(coroutineContext) {
                startMicRecording()
            }
        } else {
            isRecordingStarted = false
            AppLifecycleListener.isRecording = false
            recordingEndedLiveData.postValue(true)
            recordingStartedLiveData.postValue(false)
            recordingTimeLiveData.postValue(0)
            if (!AppLifecycleListener.isForeground) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startNotification() {
        Log.i(TAG, "$APP_NAME startNotification")

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        startForeground(NOTIFICATION_ID, getNotification())
    }

    private fun getNotification(): Notification? {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return null
        val channel = NotificationChannel(CHANNEL_ID, "MicRecordings", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManger.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(AppConstants.FROM_NOTIFICATION, true)
        val pendingIntent = PendingIntentHelper.getActivity(this, 0, intent)

        val acceptIntent = Intent(this, MicToSpeakerService::class.java)
        acceptIntent.putExtra(AppConstants.EXTRA_ACTION, AppConstants.END_RECORDING)
        val acceptPendingIntent = PendingIntentHelper.getService(this, 0, acceptIntent)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mic To Bluetooth Speaker")
            .setContentText("Recording Time: ${RecordingUtils.getFormattedRecordingTimeForNotification((recordingTimeLiveData.value?:0L)+1)}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .addAction(R.drawable.ic_mic_recording, getString(R.string.end_recording), acceptPendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, getNotification())
    }

    override fun onDestroy() {
        Log.i(TAG, "$APP_NAME onDestroy")
        recordingStartedLiveData.postValue(false)
        recordingEndedLiveData.postValue(false)
        isRecordingStarted = false
        AppLifecycleListener.isRecording = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class ServiceBinder : Binder() {
        val service : MicToSpeakerService get() = this@MicToSpeakerService
    }

    private fun startMicRecording() {
        val permissionEnabled = SharedPreferenceManager.getBooleanValue(AppConstants.SAVE_RECORDING)
        var recordingFile: File? = null
        if (permissionEnabled)
            recordingFile = AppUtils.getRecordingFile(this)

        if (mAudioOutput.state == AudioTrack.STATE_INITIALIZED && mAudioInput.state == AudioRecord.STATE_INITIALIZED) {
            var os: FileOutputStream? = null
            if (recordingFile != null) {
                try {
                    os = FileOutputStream(recordingFile)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
            try {
                mAudioOutput.play()
                try {
                    mAudioInput.startRecording()
                    try {
                        val bytes: ByteBuffer = ByteBuffer.allocateDirect(mInBufferSize)
                        val b = ByteArray(mInBufferSize)
                        while (isRecordingStarted) {
                            val o: Int = mAudioInput.read(bytes, mInBufferSize)
                            bytes.get(b)
                            bytes.rewind()
                            mAudioOutput.write(b, 0, o)
                        }
                        Log.d(TAG, "$APP_NAME Finished recording")
                    } catch (e: Exception) {
                        Log.d(TAG, "$APP_NAME Error while recording, aborting.")
                    }
                    try {
                        mAudioOutput.stop()
                        try {
                            mAudioInput.stop()
                        } catch (e: Exception) {
                            try {
                                Log.e(TAG, "$APP_NAME Can't stop recording")
                                return
                            } catch (e: Exception) {
                                Log.d(TAG, "$APP_NAME Error somewhere in record loop.")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "$APP_NAME Can't stop playback")
                        mAudioInput.stop()
                        return
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "$APP_NAME Failed to start recording")
                    mAudioOutput.stop()
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "$APP_NAME Failed to start playback")
                return
            }
        }
    }

    private fun startRecordingTimer() {
        var i = 0L
        recordingTimeLiveData.postValue(i++)
        updateNotification()
        handler.postDelayed(object : Runnable{
            override fun run() {
                Log.e(TAG, "$APP_NAME Record timer running i: $i")
                if (isRecordingStarted) {
                    recordingTimeLiveData.postValue(i++)
                    updateNotification()
                    handler.postDelayed(this, 1000L)
                }
            }
        }, 1000L)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    companion object{
        const val CHANNEL_ID = "17071995"
        const val NOTIFICATION_ID = 107
    }
}