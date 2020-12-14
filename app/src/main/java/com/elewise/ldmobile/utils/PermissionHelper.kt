package com.elewise.ldmobile.utils

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.elewise.ldmobile.R
import com.google.android.material.snackbar.Snackbar


class PermissionHelper(val activity: Activity) {

    fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(permission),
            requestCode)
    }

    fun showSnackBar(view: View, requestCode: Int) {
        val snackbar = Snackbar.make(
            view,
            R.string.snackbar_open_settings_hint,
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(R.string.action_settings, object : View.OnClickListener {
            override fun onClick(p0: View?) {
                openApplicationSettings(activity, requestCode)
            }
        })
        snackbar.show()
    }

    // открытие "Настроек" на устройстве
    private fun openApplicationSettings(activity: Activity, requestCode: Int) {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.getPackageName())
        )
        activity.startActivityForResult(appSettingsIntent, requestCode)
        Toast.makeText(activity, R.string.toast_open_settings_hint, Toast.LENGTH_LONG).show()
    }

    fun showDialogNotConfirmPermissionStorage(requestCode: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.dialog_not_granted_perm)
            .setCancelable(false)
            .setNegativeButton(R.string.dialog_btn_cancel,
                { dialog, id -> dialog.cancel() })
            .setPositiveButton(R.string.dilog_btn_request, { dialog, id -> requestPermission(WRITE_EXTERNAL_STORAGE, requestCode) })
        val alert = builder.create()
        alert.show()
    }
}