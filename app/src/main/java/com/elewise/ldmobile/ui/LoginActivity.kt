package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.AuthStatusType
import com.elewise.ldmobile.api.ParamAuthorizationResponse
import com.elewise.ldmobile.api.ParamTokenActivityCheckResponse
import com.elewise.ldmobile.api.ResponseSessionActivityStatus
import com.elewise.ldmobile.service.Prefs
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.MessageUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var progressDialog: ProgressDialog? = null
    private var dialog: AlertDialog? = null
    private lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage(getString(R.string.login_progress))
        }
        val login = findViewById<Button>(R.id.btnLogin)
        etLogin.setText(Prefs.getLastLogin(this))
        session = Session.getInstance()
        login.setOnClickListener { view: View? ->
            if (Prefs.getConnectAddress(this).isEmpty()) {
                showError(getString(R.string.not_added_connection_address))
            }

            if (TextUtils.isEmpty(etLogin.getText().toString())) {
                showError(getString(R.string.activity_login_enter_login))
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                showError(getString(R.string.activity_login_enter_password))
                return@setOnClickListener
            }
            runLogin(etLogin.getText().toString(), etPassword.getText().toString())
        }
        fbSettings.setOnClickListener({ view: View? -> startActivity(Intent(this, SettingsActivity::class.java)) })
        supportActionBar?.hide()

        // проверим активность сохраненной сессии, если есть
        val lastToken = session.token
        if (!TextUtils.isEmpty(lastToken)) {
            tokenCheck(lastToken)
        }
    }

    private fun tokenCheck(token: String) {
        progressDialog?.show()
        uiScope.launch {
            try {
                val response = session.tokenActivityCheck(token).await()
                handleTokenActivityCheck(response.body())
            } catch (e: Exception) {
                progressDialog?.dismiss()
                Log.e("erorr token check", e.toString())
            }
        }
    }

    private fun runLogin(userName: String, password: String) {
        progressDialog?.show()
        uiScope.launch {
            try {
                val request = session.getAuthToken(userName, password)
                val response = request.await()
                handleLoginResponse(response.body())
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showError(getString(R.string.error_load_data))
                Log.e("erorr login", e.toString())
            }
        }
    }

    private fun handleTokenActivityCheck(response: ParamTokenActivityCheckResponse?) {
        runOnUiThread {
            if (response != null) {
                if (response.session_activity_status != null && response.session_activity_status == ResponseSessionActivityStatus.Y.name) {
                    // сессия еще активна, перейдем сразу к документам
                    openDocsActivity()
                } else {
                    if (!TextUtils.isEmpty(response.message)) {
                        Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_LONG).show()
                    }
                    progressDialog?.cancel()
                }

                if (response.docs_size != null) {
                    session.docSize = response.docs_size
                }
            } else {
                progressDialog?.cancel()
            }
        }
    }

    private fun handleLoginResponse(response: ParamAuthorizationResponse?) {
        runOnUiThread {
            progressDialog?.cancel()

            // вполнено с ошибками
            var errorMessage: String? = getString(R.string.error_load_data)
            if (response != null) {
                if (response.status == AuthStatusType.S.name) {
                    // успешно
                    if (response.docs_size != null) {
                        session.docSize = response.docs_size
                    }

                    session.lastAuth = response
                    Prefs.saveLastLogin(this@LoginActivity, etLogin.text.toString())
                    openDocsActivity()
                    return@runOnUiThread
                } else {
                    if (!TextUtils.isEmpty(response.message)) errorMessage = response.message
                }
            }
            showError(errorMessage)
        }
    }

    private fun showError(errorMessage: String?) {
        // показать ошибку
        dialog = MessageUtils.createDialog(this@LoginActivity, getString(R.string.alert_dialog_error), errorMessage)
        dialog?.show()
    }

    private fun openDocsActivity() {
        val intent = Intent()
        intent.setClass(this@LoginActivity, DocsActivity::class.java)
        this@LoginActivity.startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        progressDialog?.dismiss()
        dialog?.dismiss()
    }
}