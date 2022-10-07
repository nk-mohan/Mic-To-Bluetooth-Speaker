package com.makeover.mictobluetoothspeaker.ui.settings

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.makeover.mictobluetoothspeaker.databinding.FragmentSettingsBinding
import com.makeover.mictobluetoothspeaker.utils.PermissionAlertDialog
import com.makeover.mictobluetoothspeaker.utils.PermissionDialogListener
import com.makeover.mictobluetoothspeaker.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var settingsDataBinding: SettingsDataBinding

    @Inject
    lateinit var permissionAlertDialog: PermissionAlertDialog

    private var _binding: FragmentSettingsBinding? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: PermissionManager.isReadFilePermissionAllowed(requireContext())
        if(readPermissionGranted) {
            settingsDataBinding.saveRecordingsEnabled = true
        }
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.settingsBindingData = settingsDataBinding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsDataBinding.setActivity(requireActivity() as AppCompatActivity)
        settingsDataBinding.setFragment(this)
        settingsViewModel.getSelectedTheme()

        setObservers()
    }

    private fun setObservers() {
        settingsViewModel.selectedTheme.observe(viewLifecycleOwner) {
            binding.themeTextView.text = settingsDataBinding.selectedTheme
        }

        settingsDataBinding.setPermissionCallBack(object : PermissionRequestCallBack {
            override fun requestStoragePermission() {
                PermissionManager.requestStoragePermission(requireActivity(), permissionAlertDialog, storagePermissionLauncher, object : PermissionDialogListener {
                    override fun onPositiveButtonClicked() {
                        //Not Needed
                    }

                    override fun onNegativeButtonClicked() {
                        settingsDataBinding.saveRecordingsEnabled = false
                    }

                })
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}