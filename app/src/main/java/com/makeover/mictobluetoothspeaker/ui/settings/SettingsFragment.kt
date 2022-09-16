package com.makeover.mictobluetoothspeaker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.makeover.mictobluetoothspeaker.databinding.FragmentSettingsBinding
import com.makeover.mictobluetoothspeaker.databinding.SettingsDataBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    @Inject
    lateinit var settingsDataBinding: SettingsDataBinding

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.settingsBindingData = settingsDataBinding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsDataBinding.setActivity(requireActivity())
        settingsDataBinding.setFragment(this)
        settingsViewModel.getSelectedTheme()

        setObservers()
    }

    private fun setObservers() {
        settingsViewModel.selectedTheme.observe(viewLifecycleOwner) {
            binding.themeTextView.text = settingsDataBinding.selectedTheme
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}