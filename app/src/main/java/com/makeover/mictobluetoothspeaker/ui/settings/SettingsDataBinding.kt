package com.makeover.mictobluetoothspeaker.ui.settings

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.makeover.mictobluetoothspeaker.BuildConfig
import com.makeover.mictobluetoothspeaker.R
import com.makeover.mictobluetoothspeaker.ui.views.AlertDialogView
import com.makeover.mictobluetoothspeaker.ui.views.ChooseOneAlertDialogListener
import com.makeover.mictobluetoothspeaker.utils.AppUtils
import com.makeover.mictobluetoothspeaker.utils.SharedPreferenceManager
import com.makeover.mictobluetoothspeaker.utils.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import androidx.databinding.library.baseAdapters.BR
import com.makeover.mictobluetoothspeaker.utils.PermissionManager


class SettingsDataBinding @Inject constructor(@ApplicationContext val context: Context) : BaseObservable() {

    @Inject
    lateinit var alertDialogView: AlertDialogView

    private var activity: AppCompatActivity? = null

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    private lateinit var permissionRequestCallBack: PermissionRequestCallBack

    private var fragment: Fragment? = null

    fun setFragment(fragment: Fragment?) {
        this.fragment = fragment
    }

    fun setPermissionCallBack(permissionRequestCallBack: PermissionRequestCallBack){
        this.permissionRequestCallBack = permissionRequestCallBack
    }

    fun changeTheme() {
        var choices: Array<String> = context.resources.getStringArray(R.array.choose_theme_choices)
        val selectedMode = SharedPreferenceManager.getIntValue(AppConstants.DAY_NIGHT_MODE)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            choices = choices.sliceArray(IntRange(0, 1))
        alertDialogView.showChooseOneAlertDialog(activity, context.resources.getString(R.string.choose_theme),
            choices, selectedMode,
            context.resources.getString(R.string.positive_button),
            context.resources.getString(R.string.negative_button),
            object : ChooseOneAlertDialogListener {
                override fun selectedItem(position: Int) {
                    SharedPreferenceManager.setIntValue(AppConstants.DAY_NIGHT_MODE, position)
                    AppCompatDelegate.setDefaultNightMode(AppUtils.getNightMode())
                }
            })
    }

    val selectedTheme
        get() = when (SharedPreferenceManager.getIntValue(AppConstants.DAY_NIGHT_MODE)) {
            AppConstants.THEME_DARK -> context.resources.getString(R.string.theme_dark)
            AppConstants.THEME_SYSTEM_DEFAULT -> context.resources.getString(R.string.theme_system_default)
            else -> context.resources.getString(R.string.theme_light)
        }

    fun enableOrDisableSaveRecordings() {
        val saveRecordings = SharedPreferenceManager.getBooleanValue(AppConstants.SAVE_RECORDING)
        if (saveRecordings) {
            saveRecordingsEnabled = false
        } else {
            if (PermissionManager.isReadFilePermissionAllowed(context)
                && PermissionManager.isWriteFilePermissionAllowed(context)) {
                saveRecordingsEnabled = true
            } else {
                permissionRequestCallBack.requestStoragePermission()
            }
        }
    }

    @get:Bindable
    var saveRecordingsEnabled = SharedPreferenceManager.getBooleanValue(AppConstants.SAVE_RECORDING)
    get() = field
    set(value) {
        field = value
        if (value)
            AppUtils.createFolderIfNotExist(AppUtils.getPath(context, context.getString(R.string.recording_folder_label)))
        SharedPreferenceManager.setBooleanValue(AppConstants.SAVE_RECORDING, value)
        notifyPropertyChanged(BR.saveRecordingsEnabled)
    }

    fun navPrivacyPolicy() {
        fragment?.findNavController()?.navigate(R.id.nav_privacy_policy)
    }

    fun versionName(): String {
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val stringBuilder = StringBuilder()
            .append("Released On: ")
            .append(dateFormat.format(BuildConfig.BUILD_TIME))
            .append(System.getProperty("line.separator"))
            .append(String.format(context.resources.getString(R.string.version_name),
                BuildConfig.VERSION_NAME))
        val versionDate = dateFormat.format(BuildConfig.BUILD_TIME)
        val versionName = BuildConfig.VERSION_NAME
        val firstIndex = stringBuilder.indexOf(versionDate)
        val secondIndex = stringBuilder.indexOf(versionName)
        val spannableString = SpannableString(stringBuilder)
        val bold = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(bold, firstIndex, versionDate.length + firstIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), firstIndex, versionDate.length + firstIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(bold, secondIndex, versionName.length + secondIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), secondIndex, versionName.length + secondIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString.toString()
    }
}