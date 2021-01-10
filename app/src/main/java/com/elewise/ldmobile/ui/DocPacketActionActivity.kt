package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FileSign
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.api.data.ButtonDesc
import com.elewise.ldmobile.criptopro.CAdESSignVerifyExample
import com.elewise.ldmobile.criptopro.OnSignedResult
import com.elewise.ldmobile.criptopro.SignedResult
import com.elewise.ldmobile.criptopro.util.ContainerAdapter
import com.elewise.ldmobile.criptopro.util.ProviderType
import com.elewise.ldmobile.service.Prefs.getContainerAlias
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.Logger.Companion.log
import com.elewise.ldmobile.utils.MessageUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_doc_packet_reject.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import ru.CryptoPro.CAdES.CAdESType
import ru.CryptoPro.JCSP.CSPConfig
import ru.CryptoPro.JCSP.support.BKSTrustStore
import ru.cprocsp.ACSP.tools.common.CSPLicenseConstants
import java.io.*

class DocPacketActionActivity : AppCompatActivity() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var dialog: AlertDialog? = null
    private var progressDialog: ProgressDialog? = null

    private lateinit var buttonDesc: ButtonDesc
    private val session = Session.getInstance()
    private lateinit var bytesData: ByteArray
    private var documentDetail = Session.getInstance().currentDocumentDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_packet_reject)
        buttonDesc = intent.getSerializableExtra(PARAM_IN_DOC_DETAIL) as ButtonDesc
        tvTitle.setText(buttonDesc.title)
        tvText.setText(buttonDesc.text)
        btnOk.setText(buttonDesc.caption)

        if (buttonDesc.comment_flag) {
            llComment.setVisibility(View.VISIBLE)
        } else {
            llComment.setVisibility(View.GONE)
        }
        btnOk.setOnClickListener { view: View ->
            if (buttonDesc.comment_flag && TextUtils.isEmpty(edComment.getText().trim().toString())) {
                Toast.makeText(this@DocPacketActionActivity, getString(R.string.dialog_refect_doc_packet_need_specify_comment),
                        Toast.LENGTH_LONG).show()
            } else {
                val providerInfo = CSPConfig.INSTANCE.cspProviderInfo
                if (providerInfo.license.existingLicenseStatus == CSPLicenseConstants.LICENSE_STATUS_OK && providerInfo.license.serialNumber != CSPLicenseConstants.CSP_50_LICENSE_DEFAULT) {
                    if (buttonDesc.sign_flag) {
                        operationBefore()
                    } else {
                        operationAfter(null)
                    }
                } else {
                    Snackbar.make(view, R.string.cripto_pro_invalid_license, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        btnCancel.setOnClickListener{ view: View? ->
            setResult(PARAM_RESULT_NOT)
            finish()
        }
    }


    private var xmlSigner: XmlSigner? = null

    private fun operationBefore() {
        showProgressDialog()
        uiScope.launch {
            var errodMessage: String? = null
            try {
                val request = session.execDocument(documentDetail.doc_id, buttonDesc.type, "before", edComment.text.toString()).await()
                val response = request.body()
                response?.let {
                    if (it.status == ResponseStatusType.S.name) {
                        if (it.file_ids != null && it.file_ids.isNotEmpty()) {
                            xmlSigner = XmlSigner(it.file_ids)
                        } else {
                            errodMessage = getString(R.string.not_find_doc_id)
                        }
                    } else {
                        if (it.message != null && it.message.isNotEmpty()) {
                            errodMessage = it.message
                        } else {
                            errodMessage = getString(R.string.error_unknown)
                        }
                    }
                } ?: run {
                    errodMessage = getString(R.string.error_load_data)
                }
            } catch (e: Exception) {
                errodMessage = getString(R.string.error_unknown)
            }

            errodMessage?.let {
                progressDialog?.dismiss()
                showError(errodMessage)
            }
        }
    }


    private inner class XmlSigner(idxList: List<Int>) {

        private val listFilesData: ArrayList<FileSign> = ArrayList()
        private lateinit var signData: FileSign


        init {
            idxList.forEach {
                listFilesData.add(FileSign(it, null))
            }
            next(null)
        }

        private fun findNextSingData() {
            var newSignData: FileSign? = null
            listFilesData.forEach forE@{
                // ищем следующий
                if (it.data == null) {
                    newSignData = it
                    return@forE
                }
            }

            if (newSignData == null) {
                operationAfter(listFilesData)
            } else {
                signData = newSignData!!

                uiScope.launch {
                    var errodMessage: String? = null
                    try {
                        val response = Session.getInstance().getFile(signData.file_id).await().body()
                        response?.let {
                            if (response.status == ResponseStatusType.S.name && response.base64?.isNotEmpty() == true) {
                                bytesData = it.base64!!.toByteArray()
                                sign()
                            } else {
                                errodMessage = "ошибка получения файла"
                            }
                        } ?: run {
                            errodMessage = "ошибка получения файла"
                        }
                    } catch (e: Exception) {
                        errodMessage = getString(R.string.error_load_data)
                    }

                    runOnUiThread {
                        errodMessage?.let {
                            progressDialog?.dismiss()
                            showError(errodMessage)
                        }
                    }
                }
            }
        }

        fun next(signature: ByteArray?) {
            if (signature != null) {
                listFilesData.forEach {
                    // сохраняем подпись
                    if (it.file_id == signData.file_id) {
                        val encoded: String = Base64.encodeToString(signature, Base64.DEFAULT)
                        it.data = encoded
                    }
                }
            }

            findNextSingData()
        }
    }

    private fun operationAfter(listSignature: List<FileSign>?) {
        showProgressDialog()
        uiScope.launch {
            var errodMessage: String? = null
            try {
                if (listSignature != null) {
                    val response = session.saveFileSign(listSignature).await().body()
                    response?.let {
                        if (it.status != ResponseStatusType.S.name) {
                            if (it.message != null && it.message.isNotEmpty()) {
                                errodMessage = it.message
                            } else {
                                errodMessage = "Ошибка отправки подписи"
                            }
                        }
                    } ?: run {
                        errodMessage = "Ошибка отправки подписи"
                    }
                }
                if (errodMessage == null) {
                    val request = session.execDocument(documentDetail.doc_id, buttonDesc.type, "after", null).await()
                    val response = request.body()
                    response?.let {
                        if (it.status == ResponseStatusType.S.name) {
                            if (it.file_ids != null && it.file_ids.isNotEmpty()) {
                                xmlSigner = XmlSigner(it.file_ids)
                            } else {
                                // все готово
                                setResult(PARAM_RESULT_OK)
                                finish()
                                return@launch
                            }
                        } else {
                            if (it.message != null && it.message.isNotEmpty()) {
                                errodMessage = it.message
                            } else {
                                errodMessage = getString(R.string.error_unknown)
                            }
                        }
                    } ?: run {
                        errodMessage = getString(R.string.error_unknown)
                    }
                }
            } catch (e: Exception) {
                errodMessage = getString(R.string.error_load_data)
            }

            runOnUiThread {
                progressDialog?.dismiss()
                errodMessage?.let {
                    showError(errodMessage)
                }
            }
        }
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                setCancelable(false)
                setMessage(getString(R.string.progress_dialog_load))
            }
        }
        progressDialog?.show()
    }

    private fun showError(errorMessage: String?) {
        dialog = MessageUtils.createDialog(this, getString(R.string.alert_dialog_error), errorMessage)
        dialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        dialog?.dismiss()
        progressDialog?.dismiss()
    }

    public override fun onResume() {
        super.onResume()

        // Необходимо для отображения диалоговых окон
        // ДСЧ, ввода пин-кода и сообщений.
        if (CSPConfig.INSTANCE != null) {
            CSPConfig.registerActivityContext(this)
        }
    }

    private fun sign() {
//        try {
        // Сборка универсального ContainerAdapter.

        // Клиентский контейнер (подписант, отправитель, TLS).
        val clientAlias = getContainerAlias(this)
        val clientPasswordSequence: CharSequence = "1"
        val clientPassword = clientPasswordSequence.toString().toCharArray()

        // Контейнер получателя.
        val serverAlias: String? = null

        // Настройки примера.
        val adapter = ContainerAdapter(this,
                clientAlias, clientPassword, serverAlias, null)
        adapter.providerType = ProviderType.currentProviderType()
        adapter.resources = resources // для примера установки сертификатов

//            final String trustStorePath = this.getApplicationInfo().dataDir +
//                    File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator +
//                    BKSTrustStore.STORAGE_FILE_TRUST;
        log("Example trust store: " + SettingsCriptoProActivity.TRUST_STORE_PATH)
        adapter.trustStoreProvider = BouncyCastleProvider.PROVIDER_NAME
        adapter.trustStoreType = BKSTrustStore.STORAGE_TYPE
        adapter.trustStoreStream = FileInputStream(SettingsCriptoProActivity.TRUST_STORE_PATH)
        adapter.trustStorePassword = BKSTrustStore.STORAGE_PASSWORD

        val example = CAdESSignVerifyExample(adapter, CAdESType.CAdES_BES, bytesData,
                object : OnSignedResult {
                    override fun signedResult(result: SignedResult) {
                        Log.e("signedResult", result.toString())
                        if (!result.succes) {
                            setResult(PARAM_RESULT_NOT)
                            finish()
                        } else {
                            xmlSigner?.next(result.signature)
                        }
                    }
                })
        example.getResult()
    }

    companion object {
        const val PARAM_RESULT_NOT = 3
        const val PARAM_RESULT_OK = 2
        const val PARAM_IN_DOC_DETAIL = "param_in_doc_detail"
    }
}