package com.elewise.ldmobile.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Prefs
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        edConnectAddress.setText(Prefs.getConnectAddress(this));

        btnSave.setOnClickListener {
            if (TextUtils.isEmpty(edConnectAddress.text.toString())) {
                Toast.makeText(this, R.string.activity_settings_error_empty, Toast.LENGTH_LONG).show()
            } else {
                Prefs.saveConnectAddress(this, edConnectAddress.text.toString())
                finish()
            }
        }

        btnOpenCriptoProSettings.setOnClickListener {
            startActivity(Intent(this, SettingsCriptoProActivity::class.java))
        }

        updateActionBar(getString(R.string.activity_settings_title))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}