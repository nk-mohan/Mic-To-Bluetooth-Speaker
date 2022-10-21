package com.makeover.mictobluetoothspeaker.ui.recordings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.makeover.mictobluetoothspeaker.databinding.RowItemRecordingFileBinding

class RecordingFileViewHolder (private var viewBinding: RowItemRecordingFileBinding, val onAudioCallback: (AudioFileData) -> Unit) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bindValues(audioFile : AudioFileData) {
        viewBinding.audioData = audioFile
        viewBinding.audioFileLayout.setOnClickListener { onAudioCallback(audioFile) }
    }

    companion object {
        fun create(parent: ViewGroup, onAudioCallback: (AudioFileData) -> Unit) : RecordingFileViewHolder {
            val binding = RowItemRecordingFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RecordingFileViewHolder(binding, onAudioCallback)
        }
    }
}