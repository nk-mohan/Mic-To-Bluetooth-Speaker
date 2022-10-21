package com.makeover.mictobluetoothspeaker.ui.recordings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class RecordingsFragment : ParentFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViews(inflater, container, loadRecordings = true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateViews()
        readFilesFromStorage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}