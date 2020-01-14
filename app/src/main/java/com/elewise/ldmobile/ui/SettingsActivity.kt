package com.elewise.ldmobile.ui

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import com.elewise.ldmobile.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity: AppCompatActivity() {
    val STORAGE_NAME = "settings"
    lateinit var storage: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storage = getSharedPreferences(STORAGE_NAME, Activity.MODE_PRIVATE)

        edConnectAddress.setText(getConnectAddress());

        btnSave.setOnClickListener {
            if (TextUtils.isEmpty(edConnectAddress.text.toString())) {
                Toast.makeText(this, R.string.activity_settings_error_empty, Toast.LENGTH_LONG).show()
            } else {
                saveConnectAddress(edConnectAddress.text.toString())
                finish()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
    private fun getConnectAddress(): String {
        return storage.getString(STORAGE_NAME, "")
    }

    private fun saveConnectAddress(address: String) {
        val edit = storage.edit()
        edit.putString(STORAGE_NAME, address)
        edit.apply()
    }
}