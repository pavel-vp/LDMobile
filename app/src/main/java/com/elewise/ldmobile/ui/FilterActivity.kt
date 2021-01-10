package com.elewise.ldmobile.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterData
import com.elewise.ldmobile.api.ParamExecOperationActionType
import com.elewise.ldmobile.api.ParamFilterSettingsResponse
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.utils.MessageUtils
import com.elewise.ldmobile.widget.*
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*


class FilterActivity : BaseActivity() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    var dialog: AlertDialog? = null

    private val dynamicViewList: MutableList<BaseWidget> = ArrayList()
    private var session = Session.getInstance()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        btnClear.setOnClickListener{ view: View? ->
            uiScope.launch {
                showProgressDialog()
                try {
                    val response = session.execOperation(ParamExecOperationActionType.reset_filters.name).await().body()
                    response?.let {
                        if (it.status == (ResponseStatusType.S.name)) {
                            loadData(true)
                        } else {
                            showError(getString(R.string.error_unknown))
                        }
                    }
                } catch (e: Exception) {
                    progressDialog?.dismiss()
                    showError()
                }
            }
        }
        btnApply.setOnClickListener{ view: View? ->
            if (validateFilterData()) {
                // fixme фильтр локально устанавливается тут, но на сервере только при отправке getDocuments
                session.filterData = filterData
                finish()
            }
        }
        llDynamicPart.setOnClickListener { hideKeyboard() }
        scrollView.setOnTouchListener { v, event ->
            hideKeyboard()
            false
        }
        updateActionBar(getString(R.string.filter_activity_title))
        loadData()
    }

    private fun loadData(withClean: Boolean = false) {
        uiScope.launch {
            try {
                showProgressDialog()
                val request = session.filterSettings
                val response = request.await()
                handleFilterSettingsResponse(response.body(), withClean)
            } catch (e: Exception) {
                progressDialog?.dismiss()
                showError()
            }
        }
    }

    private fun showError(errorMessage: String = getString(R.string.error_load_data)) {
        // показать ошибку
        dialog = MessageUtils.createDialog(this, getString(R.string.alert_dialog_error), errorMessage).apply { show() }
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

    private fun handleFilterSettingsResponse(response: ParamFilterSettingsResponse?, withClean: Boolean = false) {
        progressDialog?.cancel()
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                llDynamicPart.removeAllViews()
                dynamicViewList.clear()
                // в цикле добавляем виджеты
                response.filters?.let {
                    for (item in it) {
                        if (item.type == "date") {
                            val view = DateWidget(this, item)
                            dynamicViewList.add(view)
                            llDynamicPart.addView(view)
                        } else if (item.type == "string") {
                            val view = InputWidget(this, item)
                            dynamicViewList.add(view)
                            llDynamicPart.addView(view)
                        } else if (item.type == "list") {
                            val view = SelectWidget(this, item)
                            dynamicViewList.add(view)
                            llDynamicPart.addView(view)
                        } else if (item.type == "checkbox") {
                            val view = CheckboxWidget(this, item)
                            dynamicViewList.add(view)
                            llDynamicPart.addView(view)
                        } else {
                            Log.e("loadData", "unknown filter type")
                        }
                    }
                }
                if (withClean) {
                    // сохраним дефольтный фильтр
                    session.filterData = filterData
                }
            } else if (response.status == ResponseStatusType.E.name) {
                if (response.message?.isNotEmpty() == true) {
                    showError(response.message)
                } else {
                    showError()
                }
            } else if (response.status == ResponseStatusType.A.name) {
                session.errorAuth()
            } else {
                showError(getString(R.string.error_unknown_status_type))
            }
        } else {
            showError()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        progressDialog?.dismiss()
        dialog?.dismiss()
    }

    private val filterData: List<FilterData>
        get() {
            val arrayList: ArrayList<FilterData> = ArrayList()
            for (item in dynamicViewList) {
                arrayList.add(FilterData(item.getName(), item.getValue1(), item.getValue2()))
            }
            return arrayList
        }

    private fun validateFilterData(): Boolean {
        for (item in dynamicViewList) {
            val value = item.validate()
            if (!TextUtils.isEmpty(value)) {
                MessageUtils.createDialog(this, getString(R.string.alert_dialog_error), getString(R.string.activity_filter_validate_error, value)).show()
                return false
            }
        }
        return true
    }

    fun hideKeyboard() {
        dynamicViewList.forEach {
            if (it is InputWidget) {
                it.clearFocusEditView()
            }
        }
        hideKeyboard(llDynamicPart)
    }
}