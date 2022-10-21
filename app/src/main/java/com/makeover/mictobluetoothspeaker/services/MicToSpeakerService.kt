package com.makeover.mictobluetoothspeaker.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.*
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.makeover.mictobluetoothspeaker.AppLifecycleListener
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.TAG
import com.makeover.mictobluetoothspeaker.ui.MainActivity
import com.makeover.mictobluetoothspeaker.utils.*
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants.Companion.APP_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import android.webkit.MimeTypeMap

class MicToSpeakerService : Service(), CoroutineScope {

    private var isRecordingStarted = false

    private lateinit var mAudioInput: AudioRecord
    private lateinit var mAudioOutput   : AudioTrack
    private lateinit var mediaCodec: MediaCodec
    private lateinit var bufferInfo: MediaCodec.BufferInfo
    private lateinit var convertClass: Convert
    private lateinit var codecInputBuffers: Array<ByteBuffer>
    private lateinit var codecOutputBuffers: Array<ByteBuffer>

    private val audioSource = MediaRecorder.AudioSource.MIC // for raw audio, use MediaRecorder.AudioSource.UNPROCESSED, see note in MediaRecorder section
    private val sampleRate = 44100
    private val CHANNELS = 1
    private val BIT_RATE = 32000
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

        createAudioRecord()
        createMediaCodec()

        bufferInfo = MediaCodec.BufferInfo()
        convertClass = Convert()

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

    @SuppressLint("MissingPermission")
    private fun createAudioRecord() {
        mAudioInput = AudioRecord(audioSource, sampleRate, channelInConfig, audioFormat, mInBufferSize)

        if (mAudioInput.state != AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "Unable to initialize AudioRecord")
            throw RuntimeException("Unable to initialize AudioRecord")
        }

        if (NoiseSuppressor.isAvailable()) {
            val noiseSuppressor = NoiseSuppressor.create(mAudioInput.audioSessionId)
            if (noiseSuppressor != null) {
                noiseSuppressor.enabled = true
            }
        }

        if (AutomaticGainControl.isAvailable()) {
            val automaticGainControl = AutomaticGainControl.create(mAudioInput.audioSessionId)
            if (automaticGainControl != null) {
                automaticGainControl.enabled = true
            }
        }
    }

    @Throws(IOException::class)
    private fun createMediaCodec(): MediaCodec {
        mediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm")
        val mediaFormat = MediaFormat()
        mediaFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm")
        mediaFormat.setInteger(
            MediaFormat.KEY_SAMPLE_RATE,
            sampleRate
        )
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNELS)
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mInBufferSize)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
        mediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        try {
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: Exception) {
            Log.w(TAG, e)
            mediaCodec.release()
            throw IOException(e)
        }
        return mediaCodec
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

        startForeground(NOTIFICATION_ID, getNotification())
    }

    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(AppConstants.FROM_NOTIFICATION, true)
            val pendingIntent = PendingIntentHelper.getActivity(this, 0, intent)

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Recording Time: ${RecordingUtils.getFormattedRecordingTimeForNotification((recordingTimeLiveData.value ?: 0L) + 1)}")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        } else {
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
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Recording Time: ${RecordingUtils.getFormattedRecordingTimeForNotification((recordingTimeLiveData.value ?: 0L) + 1)}")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .addAction(R.drawable.ic_mic_recording, getString(R.string.end_recording), acceptPendingIntent)
                .build()
        }
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

        val permissionEnabled = PermissionManager.isWriteFilePermissionAllowed(this) && SharedPreferenceManager.getBooleanValue(AppConstants.SAVE_RECORDING)
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
            mediaCodec.start()

            codecInputBuffers = mediaCodec.inputBuffers
            codecOutputBuffers = mediaCodec.outputBuffers
            try {
                mAudioOutput.play()
                try {
                    mAudioInput.startRecording()
                    try {
                        val audioRecordData = ByteArray(mInBufferSize)
                        while (isRecordingStarted) {
                            val length: Int = mAudioInput.read(audioRecordData, 0, mInBufferSize)
                            if ((length == AudioRecord.ERROR_BAD_VALUE || length == AudioRecord.ERROR_INVALID_OPERATION || length != mInBufferSize) && length != mInBufferSize) {
                                Log.d(TAG, "length != BufferSize calling onRecordFailed")
                                return
                            }
                            mAudioOutput.write(audioRecordData, 0, audioRecordData.size)
                            handleCodec(length, audioRecordData, os)
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
                        mAudioInput.release()
                        return
                    }
                    if (os != null) {
                        try {
                            os.close()
                            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("aac")
                            MediaScannerConnection.scanFile(applicationContext, arrayOf(recordingFile!!.absolutePath), arrayOf(mimeType), null)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    mediaCodec.stop()
                    mediaCodec.release()
                    mAudioInput.release()
                    mAudioOutput.release()
                } catch (e: Exception) {
                    Log.e(TAG, "$APP_NAME Failed to start recording")
                    mAudioOutput.stop()
                    mAudioOutput.release()
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "$APP_NAME Failed to start playback")
                return
            }
        }
    }

    private fun handleCodec(length: Int, audioRecordData: ByteArray, os: FileOutputStream?) {
        os?.let { fileOutputStream ->
            val codecInputBufferIndex = mediaCodec.dequeueInputBuffer((10 * 1000).toLong())

            if (codecInputBufferIndex >= 0) {
                val codecBuffer: ByteBuffer = codecInputBuffers[codecInputBufferIndex]
                codecBuffer.clear()
                codecBuffer.put(audioRecordData)
                mediaCodec.queueInputBuffer(
                    codecInputBufferIndex,
                    0,
                    length,
                    0,
                    0
                )
            }

            var codecOutputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)

            while (codecOutputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (codecOutputBufferIndex >= 0) {
                    val encoderOutputBuffer = codecOutputBuffers[codecOutputBufferIndex]
                    encoderOutputBuffer.position(bufferInfo.offset)
                    encoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    if (convertClass.checkBufferInfo(bufferInfo.flags)) {
                        val header: ByteArray = convertClass.createAdtsHeader(bufferInfo.size - bufferInfo.offset)
                        fileOutputStream.write(header)
                        val data = ByteArray(encoderOutputBuffer.remaining())
                        encoderOutputBuffer[data]
                        fileOutputStream.write(data)
                    }
                    encoderOutputBuffer.clear()
                    mediaCodec.releaseOutputBuffer(codecOutputBufferIndex, false)
                } else if (codecOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    codecOutputBuffers = mediaCodec.outputBuffers
                }
                codecOutputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
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