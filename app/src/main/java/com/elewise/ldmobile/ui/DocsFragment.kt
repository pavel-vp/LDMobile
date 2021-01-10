package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.*
import com.elewise.ldmobile.model.DocType
import com.elewise.ldmobile.model.DocumentForList
import com.elewise.ldmobile.model.ProcessType
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.ImageUtils.setIcon
import com.elewise.ldmobile.utils.MessageUtils
import kotlinx.android.synthetic.main.fragment_docs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class DocsFragment : Fragment() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var processType: ProcessType? = null
    private var progressDialog: ProgressDialog? = null
    private var dialog: AlertDialog? = null
    private var session = Session.getInstance()
    private var currrentDocFrom = 0
    private lateinit var adapter: DocsAdapter
    private var lastFilterData: List<FilterData> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            processType = ProcessType.valueOf(it.getString(ARG_PAGE)!!)
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
        adapter = DocsAdapter(context!!, ArrayList())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        registerForContextMenu(lvDocs)

        lvDocs.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (lvDocs.getLastVisiblePosition() - lvDocs.getHeaderViewsCount() -
                        lvDocs.getFooterViewsCount() >= lvDocs.adapter.getCount() - 1) {
                        getDocuments(TYPE_LOAD_DATA.INCR)
                    }

                    if (lvDocs.firstVisiblePosition == 0) {
                        getDocuments(TYPE_LOAD_DATA.DECR)
                    }
                }
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })

        llProgressBar.setOnClickListener { /* this need empty */}

        // set adapter
        lvDocs.setAdapter(adapter)
    }

    override fun onStart() {
        super.onStart()
        getDocuments()
    }

    private enum class TYPE_LOAD_DATA {
        NONE, INCR, DECR
    }

    private fun getDocuments(typeLoadData: TYPE_LOAD_DATA = TYPE_LOAD_DATA.NONE) {
        uiScope.launch {
            try {
                showProgressBar(true)

                if (session.filterData.isEmpty()) {
                    loadFilterFromServer()
                }

                // определяем параметры загрузки
                val tempDocsList = arrayListOf<DocumentForList>()
                val from = when (typeLoadData) {
                    TYPE_LOAD_DATA.INCR -> {
                        tempDocsList.addAll(adapter.list)
                        currrentDocFrom += session.docSize
                        currrentDocFrom
                    }
                    TYPE_LOAD_DATA.DECR -> {
                        currrentDocFrom = 0
                        currrentDocFrom
                    }
                    else -> {
                        if (lastFilterData == session.filterData) {
                            // фильтр не изменился. не выполняем загрузку
                            showProgressBar(false)
                            return@launch
                        } else {
                            adapter.setItemsList(listOf())
                            0
                        }
                    }
                }

                // обновим сохраненное в форме значение текущего фильтра
                lastFilterData = session.filterData

                // запросим документы
                val request = session.getDocuments(from, processType, lastFilterData)
                val response = request.await().body()

                if (response != null) {
                    if (response.status == ResponseStatusType.S.name) {
                        tempDocsList.addAll(session.groupDocByDate(response.group_flag, response.contents))
                        if (tempDocsList.isNotEmpty()) {
                            adapter.setItemsList(tempDocsList)
                            if (typeLoadData == TYPE_LOAD_DATA.INCR && adapter.list.size - 1 > lvDocs.lastVisiblePosition) {
                                // скролл на одну позицию вниз с анимацией
                                lvDocs.smoothScrollToPosition(lvDocs.lastVisiblePosition + 1)
                            }
                            lvDocs.visibility = View.VISIBLE
                            tvNoResults.visibility = View.GONE
                        } else {
                            lvDocs.visibility = View.GONE
                            tvNoResults.visibility = View.VISIBLE
                        }
                    } else if (response.status == ResponseStatusType.E.name) {
                        var errorMessage = getString(R.string.error_load_data)
                        response.message?.let {
                            if (it.isNotEmpty()) errorMessage = it
                        }
                        showError(errorMessage)
                    } else if (response.status == ResponseStatusType.A.name) {
                        session.errorAuth()
                    } else {
                        showError(getString(R.string.error_unknown_status_type))
                    }
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            }

            showProgressBar(false)
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            llProgressBar.visibility = View.VISIBLE
        } else {
            llProgressBar.visibility = View.GONE
        }
    }

    @Throws(Exception::class)
    private suspend fun loadFilterFromServer() {
        // считаем фильтр с сервера
        val filterData = arrayListOf<FilterData>()
        session.filterSettings.await().body()?.let { response ->
            response.filters?.forEach {
                if (it.last_value?.isNotEmpty() == true || it.last_value2?.isNotEmpty() == true) {
                    filterData.add(FilterData(it.name, it.last_value, it.last_value2))
                }
            }
        }
        session.filterData = filterData
    }

    private fun showDocDetail(document: Document) {
        uiScope.launch {
            try {
                showProgressDialog()
                val request = Session.getInstance().getDocumentDetail(document.doc_id)
                val response = request.await()
                handleDocumentDetailsResponse(response.body())
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showError()
            }
        }
    }

    private fun handleDocumentDetailsResponse(response: ParamDocumentDetailsResponse?) {
        progressDialog?.dismiss()
        var errorMessage: String = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                Session.getInstance().currentDocumentDetail = response
                val intent = Intent()
                intent.setClass(activity!!, DocActivity::class.java)
                startActivity(intent)
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
    inner class DocsAdapter(private val context: Context, var list: List<DocumentForList>) : BaseAdapter() {
        private val sdf = SimpleDateFormat("dd.MM.yyyy")

        fun setItemsList(list: List<DocumentForList>) {
            this.list = list
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return if (list.isNotEmpty()) {
                list.size
            } else 0
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
                tvSectionTitle.text = list[position].sectionTitle
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
                if (document.attach_flag == true) {
                    imgAttache.visibility = View.VISIBLE
                } else {
                    imgAttache.visibility = View.INVISIBLE
                }
                setIcon(resources, imgDocType, document.doc_icon)
                imgAction.visibility = View.VISIBLE
                setIcon(resources, imgAction, document.action_icon)
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