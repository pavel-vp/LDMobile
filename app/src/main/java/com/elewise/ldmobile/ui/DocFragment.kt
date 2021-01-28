package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.elewise.ldmobile.BuildConfig
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.api.data.ButtonDesc
import com.elewise.ldmobile.api.data.RelatedDoc
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.ImageUtils.setIcon
import com.elewise.ldmobile.utils.MessageUtils
import kotlinx.android.synthetic.main.buttons_lines.view.*
import kotlinx.android.synthetic.main.fragment_doc_header.view.*
import kotlinx.android.synthetic.main.related_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*


class DocFragment : Fragment() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var dialog: AlertDialog? = null
    private var progressDialog: ProgressDialog? = null
    private val session = Session.getInstance()
    private var documentDetail = Session.getInstance().currentDocumentDetail

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_doc_header, container, false)
        addButtons(rootView.llButtons)
        var isFirst = true
        documentDetail.header_attributes?.let {
            for ((desc, value, icon) in it) {
                val convertView = inflater.inflate(R.layout.doc_header_item, container, false)
                val tvDesc = convertView.findViewById<TextView>(R.id.tvDesc)
                val tvValue = convertView.findViewById<TextView>(R.id.tvValue)
                if (isFirst) {
                    val ivDocType = convertView.findViewById<ImageView>(R.id.ivDocType)
                    ivDocType.visibility = View.VISIBLE
                    setIcon(resources, ivDocType, icon)
                    isFirst = false
                }
                tvDesc.text = desc
                tvValue.text = value
                rootView.llDynamicPart.addView(convertView)
            }
        }

        documentDetail.related_docs?.let {
            for (item in it) {
                val convertView = inflater.inflate(R.layout.related_item, container, false)
                setIcon(resources, convertView.ivDocType, item.doc_icon)
                setIcon(resources, convertView.ivActionType, item.action_icon)
                convertView.tvName.text = item.doc_name
                if (item == it.first()) {
                    convertView.tvTitle.visibility = View.VISIBLE
                } else {
                    convertView.tvTitle.visibility = View.GONE
                }
                convertView.setOnClickListener { view: View ->
                    showRelatedDoc(item)
                }
                rootView.llDynamicPart.addView(convertView)
            }
        }

        documentDetail.attachments?.let {
            for (item in it) {
                val convertView = inflater.inflate(R.layout.attachment_item, container, false)
                val tvAttacheName = convertView.findViewById<TextView>(R.id.tvAttacheName)
                tvAttacheName.text = item.file_name
                convertView.setOnClickListener { view: View -> showAttachment(item.file_id) }
                if (item == it.first()) {
                    convertView.tvTitle.visibility = View.VISIBLE
                } else {
                    convertView.tvTitle.visibility = View.GONE
                }
                rootView.llDynamicPart.addView(convertView)
            }
        }
        return rootView
    }

    private fun addButtons(llButtons: LinearLayout) {
        documentDetail.buttons?.let {
            if (it.isNotEmpty()) {
                llButtons.visibility = View.VISIBLE
                var i = 0
                it.forEach { desc ->
                    val currentIndex = it.indexOf(desc)
                    if (currentIndex % 2 == 0) {
                        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val buttonsLines = inflater.inflate(R.layout.buttons_lines, null, false)
                        with (it.get(currentIndex)) {
                            buttonsLines.btnOne.visibility = View.VISIBLE
                            buttonsLines.btnOne.text = this.caption
                            buttonCreateClickListener(buttonsLines.btnOne, this)
                        }
                        if (it.size - 1 >= currentIndex + 1) {
                            with (it.get(currentIndex + 1)) {
                                buttonsLines.btnTwo.visibility = View.VISIBLE
                                buttonsLines.btnTwo.text = this.caption
                                buttonCreateClickListener(buttonsLines.btnTwo, this)
                            }
                        }
                        llButtons.addView(buttonsLines)
                    }
                }
            } else {
                llButtons.visibility = View.GONE
            }
        } ?: run {
            llButtons.visibility = View.GONE
        }
    }

    private fun buttonCreateClickListener(button: Button, buttonDesc: ButtonDesc) {
        button.setOnClickListener {
            val intent = Intent(context, DocActionActivity::class.java)
            intent.putExtra(DocActionActivity.PARAM_IN_DOC_DETAIL, buttonDesc)
            startActivityForResult(intent, REQUEST_ONE_CODE)
        }
    }

    private fun showAttachment(file_id: Int?) {
        if (file_id == null) return

        showProgressDialog()
        uiScope.launch {
            var errodMessage: String? = null
            try {
                val response = Session.getInstance().getFile(file_id).await().body()
                response?.let {
                    if (response.status == ResponseStatusType.S.name && response.base64?.isNotEmpty() == true) {
                        saveFile(response.base64, response.file_name)?.let { file ->
//                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                            // Get URI and MIME type of file
                            val uri = FileProvider.getUriForFile(activity!!, BuildConfig.APPLICATION_ID + ".fileprovider", file)
                            val mime: String? = activity!!.getContentResolver().getType(uri)

                            // Open file with user selected app
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.setDataAndType(uri, mime)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            val packageManager = activity!!.getPackageManager()
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            } else {
                                errodMessage = getString(R.string.not_find_app)
                            }
                        } ?: run {
                            errodMessage = getString(R.string.error_unknown)
                        }
                    } else {
                        errodMessage = getString(R.string.error_unknown)
                    }
                } ?: run {
                    errodMessage = getString(R.string.error_load_data)
                }

            } catch (e: Exception) {
                errodMessage = getString(R.string.error_load_data)
            }

            progressDialog?.dismiss()

            errodMessage?.let {
                showError(it)
            }
        }
    }

    private fun showRelatedDoc(relatedDoc: RelatedDoc) {
        uiScope.launch {
            try {
                showProgressDialog()
                val request = session.getDocumentDetail(relatedDoc.doc_id)
                val response = request.await()
                handleDocumentDetailsResponse(response.body());
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showError()
            }
        }
    }

    private fun handleDocumentDetailsResponse(response: ParamDocumentDetailsResponse?) {
        progressDialog?.cancel()
        var errorMessage: String? = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                session.currentDocumentDetail = response
                val intent = Intent()
                intent.setClass(activity!!, DocActivity::class.java)
                startActivity(intent)
                return
            } else if (response.status == ResponseStatusType.E.name) {
                if (!TextUtils.isEmpty(response.message)) {
                    errorMessage = response.message
                }
            } else if (response.status == ResponseStatusType.A.name) {
                session.errorAuth()
                return
            } else {
                errorMessage = getString(R.string.error_unknown_status_type)
            }
        }
        errorMessage?.let {
            showError(errorMessage)
        }
    }

    private fun showError(errorMessage: String = getString(R.string.error_load_data)) {
        dialog = MessageUtils.createDialog(activity, getString(R.string.alert_dialog_error), errorMessage)
        dialog?.show()
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity).apply {
                setCancelable(false)
                setMessage(getString(R.string.progress_dialog_load))
            }
        }
        progressDialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ONE_CODE) {
            if (resultCode == DocActionActivity.PARAM_RESULT_OK) {
                Toast.makeText(context, R.string.action_exec_success, Toast.LENGTH_LONG).show()
                activity!!.finish()
            } else {
                data?.extras?.getString(DocActionActivity.PARAM_RESULT_MESSAGE)?.let {
                    showError(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        progressDialog?.dismiss()
        dialog?.dismiss()
    }

    private fun saveFile(imageData: String, fileName: String): File? {
        val imgBytesData = Base64.decode(imageData,
                Base64.DEFAULT)

        val path = Environment.getExternalStorageDirectory().path + File.separator + "myFolder" + File.separator //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val fileDir = File(path)
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }

        val file = File(fileDir, fileName)
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
        val bufferedOutputStream = BufferedOutputStream(
                fileOutputStream)
        try {
            bufferedOutputStream.write(imgBytesData)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                bufferedOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    companion object {
        const val REQUEST_ONE_CODE = 101
    }
}