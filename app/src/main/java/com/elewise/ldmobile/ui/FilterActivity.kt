package com.elewise.ldmobile.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterData
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

    private val dynamicViewList: MutableList<BaseWidget?> = ArrayList()
    private var session = Session.getInstance()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        btnClear.setOnClickListener(View.OnClickListener { view: View? ->
            session.filterData = arrayOfNulls(0)
            finish()
        })
        btnApply.setOnClickListener(View.OnClickListener { view: View? ->
            if (validateFilterData()) {
                session.filterData = filterData
                finish()
            }
        })
        updateActionBar(getString(R.string.filter_activity_title))
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        uiScope.launch {
            showProgressDialog()
            val request = session.filterSettings
            val response = request.await()
            handleFilterSettingsResponse(response.body())
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

    private fun handleFilterSettingsResponse(response: ParamFilterSettingsResponse?) {
        progressDialog?.cancel()
        var errorMessage = getString(R.string.error_load_data)
        if (response != null) {
            if (response.status == ResponseStatusType.S.name) {
                // в цикле добавляем виджеты
                for (item in response.filters) {
                    if (item.type == "date") {
                        val view = DateWidget(this, item)
                        dynamicViewList.add(view)
                        llDynamicPart!!.addView(view)
                    } else if (item.type == "string") {
                        val view = InputWidget(this, item)
                        dynamicViewList.add(view)
                        llDynamicPart!!.addView(view)
                    } else if (item.type == "list") {
                        val view = SelectWidget(this, item)
                        dynamicViewList.add(view)
                        llDynamicPart!!.addView(view)
                    } else if (item.type == "checkbox") {
                        val view = CheckboxWidget(this, item)
                        dynamicViewList.add(view)
                        llDynamicPart!!.addView(view)
                    } else {
                        Log.e("loadData", "uncnown filter type")
                    }
                }
                return
            } else if (response.status == ResponseStatusType.E.name) {
                if (!TextUtils.isEmpty(response.message)) {
                    errorMessage = response.message!!
                }
            } else if (response.status == ResponseStatusType.A.name) {
                session.errorAuth()
                return
            } else {
                errorMessage = getString(R.string.error_unknown_status_type)
            }
        }
        if (!TextUtils.isEmpty(errorMessage)) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
        progressDialog?.dismiss()
    }

    private val filterData: Array<FilterData>
        get() {
            val arrayList: ArrayList<FilterData> = ArrayList()
            for (item in dynamicViewList) {
                if (!TextUtils.isEmpty(item!!.getValue1())) {
                    arrayList.add(FilterData(item.getName(), item.getValue1(), item.getValue2()))
                }
            }
            return arrayList.toTypedArray()
        }

    private fun validateFilterData(): Boolean {
        for (item in dynamicViewList) {
            val value = item!!.validate()
            if (!TextUtils.isEmpty(value)) {
                MessageUtils.createDialog(this, getString(R.string.alert_dialog_error), getString(R.string.activity_filter_validate_error, value)).show()
                return false
            }
        }
        return true
    }
}