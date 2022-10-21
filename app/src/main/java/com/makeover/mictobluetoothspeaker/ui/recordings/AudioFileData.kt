package com.makeover.mictobluetoothspeaker.ui.recordings

data class AudioFileData(
    val fileName: String,
    val mimeType: String,
    val fileDate: Long,
    val fileSize: String,
    val fileDuration: String,
    val pathName: String
)