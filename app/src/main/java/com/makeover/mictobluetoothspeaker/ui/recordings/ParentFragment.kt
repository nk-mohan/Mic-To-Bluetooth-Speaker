package com.makeover.mictobluetoothspeaker.ui.recordings

import android.Manifest
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.TAG
import com.makeover.mictobluetoothspeaker.databinding.FragmentMyRecordingsBinding
import com.makeover.mictobluetoothspeaker.ui.views.AudioAlertDialog
import com.makeover.mictobluetoothspeaker.utils.AppUtils
import com.makeover.mictobluetoothspeaker.utils.PermissionAlertDialog
import com.makeover.mictobluetoothspeaker.utils.PermissionDialogListener
import com.makeover.mictobluetoothspeaker.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
open class ParentFragment : Fragment(), CoroutineScope {

    protected var _binding: FragmentMyRecordingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    @Inject
    lateinit var permissionAlertDialog: PermissionAlertDialog

    @Inject
    lateinit var audioAlertDialog: AudioAlertDialog

    private var audioFilesList = arrayListOf<AudioFileData>()

    private val recordingFileAdapter = RecordingFileAdapter()

    private var fromRecordings = true

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: PermissionManager.isReadFilePermissionAllowed(requireContext())
        if(readPermissionGranted) {
            launch {
                if (fromRecordings) readRecordingFiles() else readAllAudioFiles()
            }
        }
    }

    protected fun initViews(inflater: LayoutInflater, container: ViewGroup?, loadRecordings: Boolean) {
        _binding = FragmentMyRecordingsBinding.inflate(inflater, container, false)
        audioFilesList = arrayListOf()
        fromRecordings = loadRecordings
    }

    protected fun updateViews() {
        binding.recordingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordingFileAdapter
        }

        recordingFileAdapter.onAudioCallback {
            audioAlertDialog.playAudioFile(it)
        }
    }

    protected fun readFilesFromStorage() {
        if (PermissionManager.isReadFilePermissionAllowed(requireContext())
            && PermissionManager.isWriteFilePermissionAllowed(requireContext())) {
            launch {
                if (fromRecordings) readRecordingFiles() else readAllAudioFiles()
            }
        } else {
            PermissionManager.requestStoragePermission(requireActivity(), permissionAlertDialog, storagePermissionLauncher, object :
                PermissionDialogListener {
                override fun onPositiveButtonClicked() {
                    //Not Needed
                }

                override fun onNegativeButtonClicked() {
                    // Not needed
                }
            })
        }
    }

    private fun readRecordingFiles() {
        val uri = AppUtils.getAudioMediaUri()
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATA
        )
        val order = MediaStore.Audio.AudioColumns.DATE_MODIFIED
        val cursor: Cursor? = requireContext().contentResolver.query(uri, projection, null, null, order)

        if (cursor != null) {
            Log.d(TAG, "cursor not null")
            if (cursor.moveToLast()) {
                val recordingsPath = AppUtils.getPath(requireContext(), requireContext().getString(R.string.recording_folder_label))
                do {
                    val displayName = cursor.getString(0)
                    val mimeType = cursor.getString(1)
                    val fileDate = cursor.getLong(2)
                    val fileSize = AppUtils.getStringSizeLengthFile(cursor.getLong(3))
                    val fileDuration = AppUtils.getAudioDuration(cursor.getLong(4))
                    val pathName = cursor.getString(5)
                    if (pathName.contains(recordingsPath)) {
                        Log.d(TAG, "Display name: ${displayName}, mimeType: ${mimeType}, fileDate: $fileDate, fileSize:${fileSize}, fileDuration:${fileDuration}, pathName:${pathName}")
                        audioFilesList.add(AudioFileData(displayName, mimeType, fileDate, fileSize, fileDuration, pathName))
                    }
                } while (cursor.moveToPrevious())
                cursor.close()
                launch(mainDispatcher) {
                    recordingFileAdapter.setAudioList(audioFilesList)
                }
            }
        }
    }

    private fun readAllAudioFiles() {
        val uri = AppUtils.getAudioMediaUri()
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATA
        )
        val order = MediaStore.Audio.AudioColumns.DATE_MODIFIED
        val cursor: Cursor? = requireContext().contentResolver.query(uri, projection, null, null, order)

        if (cursor != null) {
            Log.d(TAG, "cursor not null")
            if (cursor.moveToLast()) {
                do {
                    val displayName = cursor.getString(0)
                    val mimeType = cursor.getString(1)
                    val fileDate = cursor.getLong(2)
                    val fileSize = AppUtils.getStringSizeLengthFile(cursor.getLong(3))
                    val fileDuration = AppUtils.getAudioDuration(cursor.getLong(4))
                    val pathName = cursor.getString(5)
                    Log.d(TAG, "Display name: ${displayName}, mimeType: ${mimeType}, fileDate: $fileDate, fileSize:${fileSize}, fileDuration:${fileDuration}, pathName:${pathName}")
                    audioFilesList.add(AudioFileData(displayName, mimeType, fileDate, fileSize, fileDuration, pathName))
                } while (cursor.moveToPrevious())
                cursor.close()
                launch(mainDispatcher) {
                    recordingFileAdapter.setAudioList(audioFilesList)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioAlertDialog.closeAlertDialog()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

}