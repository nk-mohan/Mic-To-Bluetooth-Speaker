package com.makeover.mictobluetoothspeaker.databinding

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatDelegate
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


class SettingsDataBinding @Inject constructor(@ApplicationContext val context: Context) {

    @Inject
    lateinit var alertDialogView: AlertDialogView

    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    private var fragment: Fragment? = null

    fun setFragment(fragment: Fragment?) {
        this.fragment = fragment
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

    fun navPrivacyPolicy() {
        fragment?.findNavController()?.navigate(R.id.nav_privacy_policy)
    }

    fun versionName(): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val stringBuilder = StringBuilder()
            .append("Released On: ")
            .append(dateFormat.format(BuildConfig.BUILD_TIME))
            .append(System.getProperty("line.separator"))
            .append(String.format(context.resources.getString(R.string.version_name),
                BuildConfig.VERSION_NAME))
        val versiondate = dateFormat.format(BuildConfig.BUILD_TIME)
        val versionname = BuildConfig.VERSION_NAME
        val firstindex = stringBuilder.indexOf(versiondate)
        val secondindex = stringBuilder.indexOf(versionname)
        val spannableString = SpannableString(stringBuilder)
        val bold = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(bold, firstindex, versiondate.length + firstindex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), firstindex, versiondate.length + firstindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(bold, secondindex, versionname.length + secondindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), secondindex, versionname.length + secondindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString.toString()
    }
}