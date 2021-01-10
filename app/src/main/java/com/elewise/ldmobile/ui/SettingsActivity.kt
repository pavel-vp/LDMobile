package com.elewise.ldmobile.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Prefs
import com.elewise.ldmobile.utils.PermissionHelper
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity: BaseActivity() {

    private lateinit var permissionHelper: PermissionHelper
    private val PERMISSIONS_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        permissionHelper = PermissionHelper(this)

        edConnectAddress.setText(Prefs.getConnectAddress(this));

        btnSave.setOnClickListener {
            if (TextUtils.isEmpty(edConnectAddress.text.toString().trim())) {
                Toast.makeText(this, R.string.activity_settings_error_empty, Toast.LENGTH_LONG).show()
            } else {
                Prefs.saveConnectAddress(this, edConnectAddress.text.toString())
                finish()
            }
        }

        btnOpenCriptoProSettings.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionHelper.requestPermission(WRITE_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_CODE)
            } else {
                openCriptoProSettings()
            }
        }

        updateActionBar(getString(R.string.activity_settings_title))
    }

    private fun openCriptoProSettings() {
        startActivity(Intent(this, SettingsCriptoProActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {
                    permissions.forEach {
                        val grandResult = grantResults[permissions.indexOf(it)]
                        if (grandResult == PackageManager.PERMISSION_GRANTED) {
                            openCriptoProSettings()
                        } else {
                            if (!shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                permissionHelper.showSnackBar(btnSave, PERMISSIONS_REQUEST_CODE)
                            } else {
                                permissionHelper.showDialogNotConfirmPermissionStorage(PERMISSIONS_REQUEST_CODE)
                            }
                        }
                    }
                }
            }
        }
    }
}