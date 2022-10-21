package com.makeover.mictobluetoothspeaker.ui.recordings

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.makeover.mictobluetoothspeaker.TAG

class RecordingFileAdapter : RecyclerView.Adapter<RecordingFileViewHolder>() {

    private val audioFileList: ArrayList<AudioFileData> = arrayListOf()

    fun setAudioList(audioList: ArrayList<AudioFileData>) {
        Log.d(TAG, "setAudioList audioFilesList size: ${audioList.size}")
        audioFileList.clear()
        audioFileList.addAll(audioList)
        notifyItemRangeInserted(0, audioList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingFileViewHolder {
        return RecordingFileViewHolder.create(parent, onAudioCallback)
    }

    override fun onBindViewHolder(holder: RecordingFileViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder audioFilesList position: $position")
        val audioFile = audioFileList[position]
        holder.bindValues(audioFile)
    }

    override fun getItemCount(): Int {
        return audioFileList.size
    }


    fun onAudioCallback(fn: (AudioFileData) -> Unit) {
        onAudioCallback = fn
    }

    companion object {

        lateinit var onAudioCallback: (AudioFileData) -> Unit
    }

}