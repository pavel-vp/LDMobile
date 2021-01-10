package com.elewise.ldmobile.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elewise.ldmobile.R
import com.elewise.ldmobile.criptopro.LicenseNewExample
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_input_license_number.*
import ru.CryptoPro.JCSP.CSPConfig
import java.lang.Exception

class InputLicenseNumberActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_license_number)

        btnCancel.setOnClickListener {
            finish()
        }

        btnOk.setOnClickListener {
            var newLicense = edLicenseNumber.text.toString().trim().replace("-", "")

            if (newLicense.count() != 25) {
                Snackbar.make(btnOk, R.string.frm_input_license_incorrect, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var i = 0
            while (i < 4) {
                val separatorPosition = 5*(i + 1) + i
                newLicense = newLicense.substring(0, separatorPosition)  + "-" + newLicense.substring(separatorPosition, newLicense.count())
                i++
            }

            try {
                val providerInfo = CSPConfig.INSTANCE.cspProviderInfo
                val licenseNewExample = LicenseNewExample(providerInfo, newLicense)
                if (licenseNewExample.execute()) {
                    Toast.makeText(this, R.string.frm_input_license_result_ok, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Snackbar.make(btnOk, R.string.frm_input_license_result_fail, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(btnOk, R.string.frm_input_license_result_fail, Snackbar.LENGTH_LONG).show()
                Log.e("error", "instal license fail")
            }
        }
    }
}