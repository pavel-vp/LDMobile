package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.Document
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse
import com.elewise.ldmobile.api.ParamDocumentsResponse
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.model.DocType
import com.elewise.ldmobile.model.DocumentForList
import com.elewise.ldmobile.model.ProcessType
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.ImageUtils.setIcon
import com.elewise.ldmobile.utils.MessageUtils
import kotlinx.android.synthetic.main.fragment_docs.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class DocsFragment : Fragment() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var processType: ProcessType? = null
    private var progressDialog: ProgressDialog? = null
    var dialog: AlertDialog? = null
    var session = Session.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            processType = ProcessType.valueOf(it.getString(ARG_PAGE))
        }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_docs, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(lvDocs)
        // set adapter
        val adapter = DocsAdapter(context!!, ArrayList())
        lvDocs.setAdapter(adapter)
    }

    override fun onStart() {
        super.onStart()
        getDocuments()
    }

    private fun getDocuments() {
        uiScope.launch {
            showProgressDialog()

            val filterData = session.filterData
            val request = session.getDocuments(10, 0,
                    processType, filterData)
            try {
                val response = request.await()
                handleDocumentsResponse(response.body())
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showError()
            }
        }
    }

    private fun handleDocumentsResponse(response: ParamDocumentsResponse?) {
        progressDialog?.dismiss()
        var errorMessage: String = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                val docsList = session.groupDocByDate(response.contents)
                if (docsList != null && !docsList.isEmpty()) {
                    (lvDocs.adapter as DocsAdapter).setList(docsList)
                    lvDocs.visibility = View.VISIBLE
                    tvNoResults.visibility = View.GONE
                } else {
                    lvDocs.visibility = View.GONE
                    tvNoResults.visibility = View.VISIBLE
                }
                return
            } else if (response.status == ResponseStatusType.E.name) {
                response.message?.let { if (it.isNotEmpty()) errorMessage = it }
            } else if (response.status == ResponseStatusType.A.name) {
                session.errorAuth()
                return
            } else {
                errorMessage = getString(R.string.error_unknown_status_type)
            }
            showError(errorMessage)
        }
    }

    private fun showDocDetail(document: Document) {
        uiScope.launch {
            showProgressDialog()
            val request = Session.getInstance().getDocumentDetail(document.doc_id)
            val response = request.await()
            handleDocumentDetailsResponse(response.body())
        }
    }

    private fun handleDocumentDetailsResponse(response: ParamDocumentDetailsResponse?) {
        progressDialog?.dismiss()
        var errorMessage: String = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                Session.getInstance().currentDocumentDetail = response
                if (response.doc_alt_type == DocType.PD.name) {
                    val intent = Intent()
                    intent.setClass(activity, DocPacketActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent()
                    intent.setClass(activity, DocActivity::class.java)
                    startActivity(intent)
                }
                return
            } else if (response.status == ResponseStatusType.E.name) {
                response.message?.let { if (it.isNotEmpty()) errorMessage = it }
            } else if (response.status == ResponseStatusType.A.name) {
                session.errorAuth()
                return
            } else {
                errorMessage = getString(R.string.error_unknown_status_type)
            }
        }
        showError(errorMessage)
    }

    private fun showError(errorMessage: String = getString(R.string.error_load_data)) {
        // показать ошибку
        dialog = MessageUtils.createDialog(activity, getString(R.string.alert_dialog_error), errorMessage).apply { show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        progressDialog?.dismiss()
        dialog?.dismiss()
    }

    /**
     * Adapter
     */
    inner class DocsAdapter(private val context: Context, private var list: List<DocumentForList>) : BaseAdapter() {
        private val sdf = SimpleDateFormat("dd.MM.yyyy")
        fun setList(list: List<DocumentForList>) {
            this.list = list
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (list[position].isSection) {
                // if section header
                view = inflater.inflate(R.layout.list_group_date, parent, false)
                val tvSectionTitle = view.findViewById<TextView>(R.id.tvSectionTitle)
                tvSectionTitle.text = String.format(getString(R.string.docs_activity_date_group), list[position].sectionTitle)
                if (sdf.format(Date()) == list[position].sectionTitle) {
                    tvSectionTitle.setTypeface(null, Typeface.BOLD)
                    tvSectionTitle.setTextColor(resources.getColor(R.color.colorPrimary))
                } else {
                    tvSectionTitle.setTypeface(null, Typeface.NORMAL)
                    tvSectionTitle.setTextColor(resources.getColor(R.color.colorAccent))
                }
            } else {
                // if list
                view = inflater.inflate(R.layout.list_doc_item, parent, false)
                val tvDocTitle = view.findViewById<TextView>(R.id.tvDocTitle)
                val tvDocBody = view.findViewById<TextView>(R.id.tvDocBody)
                val imgDocType = view.findViewById<ImageView>(R.id.imgDocType)
                val imgAttache = view.findViewById<ImageView>(R.id.imgAttache)
                val imgAction = view.findViewById<ImageView>(R.id.imgAction)
                val document = list[position].document
                tvDocTitle.text = document.contractor
                tvDocBody.text = document.doc_name
                if (document.attach_flag) {
                    imgAttache.visibility = View.VISIBLE
                } else {
                    imgAttache.visibility = View.INVISIBLE
                }
                setIcon(resources, imgDocType, document.doc_icon)
                if (document.doc_alt_type == DocType.PD.name) {
                    imgAction.visibility = View.GONE
                } else {
                    imgAction.visibility = View.VISIBLE
                    setIcon(resources, imgAction, document.action_icon)
                }
                view.setOnClickListener { v: View? -> showDocDetail(document) }
            }
            return view
        }
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        @JvmStatic
        fun newInstance(processType: ProcessType): DocsFragment {
            val args = Bundle()
            args.putString(ARG_PAGE, processType.name)
            val fragment = DocsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}