package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.api.data.RelatedDoc
import com.elewise.ldmobile.model.DocType
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.ImageUtils.setIcon
import com.elewise.ldmobile.utils.MessageUtils
import kotlinx.android.synthetic.main.fragment_doc_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DocPacketFragment : Fragment() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var dialog: AlertDialog? = null
    private var progressDialog: ProgressDialog? = null
    private var session: Session = Session.getInstance()
    var documentDetail = session.currentDocumentDetail!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_doc_packet_header, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addButtons(llButtons)
        btnOne.setOnClickListener {
            // todo Есть такой скрин в Figma. На какой параметр ориентироваться
            val intent = Intent(context, DocPacketActionActivity::class.java)
            intent.putExtra(DocPacketActionActivity.PARAM_IN_DOC_DETAIL, documentDetail.buttons[0])
            startActivityForResult(intent, REQUEST_ONE_CODE)
        }
        btnTwo.setOnClickListener {
            val intent = Intent(context, DocPacketActionActivity::class.java)
            intent.putExtra(DocPacketActionActivity.PARAM_IN_DOC_DETAIL, documentDetail.buttons[1])
            startActivityForResult(intent, REQUEST_TWO_CODE)
        }
        addDynamicPart(layoutInflater, documentDetail, llDynamicPart)
        addAttachments(layoutInflater, view, documentDetail)
    }

    private fun addButtons(llButtons: LinearLayout) {
        if (documentDetail.buttons.size > 0) {
            llButtons.visibility = View.VISIBLE
            var i = 0
            while (i < documentDetail.buttons.size) {
                val (_, caption) = documentDetail.buttons[i]
                if (i == 0) {
                    btnOne.text = caption
                }
                if (i == 1) {
                    btnTwo.text = caption
                }
                i++
            }
        } else {
            llButtons.visibility = View.GONE
        }
    }

    private fun addAttachments(inflater: LayoutInflater, rootView: View, detail: ParamDocumentDetailsResponse) {
        val lvAttachemnt = rootView.findViewById<LinearLayout>(R.id.lvRelated)
        val tvRelatedHeader = rootView.findViewById<TextView>(R.id.tvRelatedHeader)
        if (detail.related_docs != null) {
            tvRelatedHeader.visibility = View.VISIBLE
            lvAttachemnt.visibility = View.VISIBLE
            for (item in detail.related_docs) {
                val convertView = inflater.inflate(R.layout.related_item, lvAttachemnt, false)
                val ivDocType = convertView.findViewById<ImageView>(R.id.ivDocType)
                val ivActionType = convertView.findViewById<ImageView>(R.id.ivActionType)
                val tvName = convertView.findViewById<TextView>(R.id.tvName)
                setIcon(resources, ivDocType, item.doc_icon)
                setIcon(resources, ivActionType, item.action_icon)
                tvName.text = item.doc_name
                convertView.tag = item
                convertView.setOnClickListener { view: View ->
                    val relatedDoc = view.tag as RelatedDoc
                    showDocDetail(relatedDoc)
                }
                lvAttachemnt.addView(convertView)
            }
        } else {
            tvRelatedHeader.visibility = View.GONE
            lvAttachemnt.visibility = View.GONE
        }
    }

    private fun addDynamicPart(inflater: LayoutInflater, detail: ParamDocumentDetailsResponse, llDynamicPart: LinearLayout) {
        var isFirst = true
        for ((desc, value) in detail.header_attributes) {
            val convertView = inflater.inflate(R.layout.doc_header_item, llDynamicPart, false)
            val tvDesc = convertView.findViewById<TextView>(R.id.tvDesc)
            val tvValue = convertView.findViewById<TextView>(R.id.tvValue)
            if (isFirst) {
                val ivDocType = convertView.findViewById<ImageView>(R.id.ivDocType)
                ivDocType.visibility = View.VISIBLE
                setIcon(resources, ivDocType, session.currentDocumentDetail.doc_icon)
                isFirst = false
            }
            tvDesc.text = desc
            tvValue.text = value
            llDynamicPart.addView(convertView)
        }
    }

    private fun showDocDetail(relatedDoc: RelatedDoc) {
        uiScope.launch {
            showProgressDialog()
            val request = session.getDocumentDetail(relatedDoc.doc_id)
            val response = request.await()
            handleDocumentDetailsResponse(response.body());
        }
    }

    private fun handleDocumentDetailsResponse(response: ParamDocumentDetailsResponse?) {
        progressDialog?.cancel()
        var errorMessage: String? = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                session.currentDocumentDetail = response
                if (response.doc_alt_type == DocType.PD.name) {
                    val intent = Intent()
                    intent.setClass(activity!!, DocPacketActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent()
                    intent.setClass(activity!!, DocActivity::class.java)
                    startActivity(intent)
                }
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
        showError(errorMessage)
    }

    private fun showError(errorMessage: String?) {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        dialog?.dismiss()
        progressDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ONE_CODE) {
            if (resultCode == DocPacketActionActivity.PARAM_RESULT_OK) {
                Toast.makeText(context, R.string.action_success, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, R.string.action_error, Toast.LENGTH_LONG).show()
            }
        } else {
            if (requestCode == REQUEST_TWO_CODE) {
                if (resultCode == DocPacketActionActivity.PARAM_RESULT_OK) {
                    Toast.makeText(context, R.string.action_success, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.action_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val REQUEST_ONE_CODE = 101
        const val REQUEST_TWO_CODE = 102
        @JvmStatic
        fun newInstance(): DocPacketFragment {
            return DocPacketFragment()
        }
    }
}