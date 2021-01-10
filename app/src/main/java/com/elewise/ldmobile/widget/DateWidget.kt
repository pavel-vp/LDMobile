package com.elewise.ldmobile.widget

import android.app.DatePickerDialog
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
import com.elewise.ldmobile.ui.FilterActivity
import kotlinx.android.synthetic.main.date_widget.view.*
import java.text.SimpleDateFormat
import java.util.*

class DateWidget(activity: FilterActivity, val descrView: FilterElement): BaseWidget(activity) {

    private val cldr = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd.MM.yyyy")

    private val dateClickListener = { view: View ->
        activity.hideKeyboard()
        val date: TextView? = view.findViewById(R.id.tvDateFrom)
        val tvDate = if (date == null) view.findViewById(R.id.tvDateTo) else date

        try {
            cldr.time = sdf.parse(tvDate.getText().toString())
        } catch (e: Exception) {
            Log.e("error parse date", e.toString())
        }

        // date picker dialog
        DatePickerDialog(context,
                { v, year1, monthOfYear, dayOfMonth ->
                    val day = (if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth).toString()
                    val month = with(monthOfYear+1) {
                        (if (this < 10) "0$this" else this.toString()).toString()
                    }
                    tvDate.setText("$day.$month.$year1") }
                , cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.date_widget, this)
        tvDateFromName.text = descrView.desc
        rlDateFrom.setOnClickListener(dateClickListener)

        tvDateToName.text = descrView.desc2
        rlDateTo.setOnClickListener(dateClickListener)

        descrView.last_value?.let {
            setValue1(it)
        }

        descrView.last_value2?.let {
            setValue2(it)
        }
    }

    override fun setValue1(data: String) {
        tvDateFrom.text = data
    }

    override fun setValue2(data: String) {
        tvDateTo.text = data
    }

    override fun getValue1(): String {
        return tvDateFrom.text.toString()
    }

    override fun getValue2(): String {
        return tvDateTo.text.toString()
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        if (descrView.required && TextUtils.isEmpty(tvDateFrom.text.toString())) {
            return descrView.desc
        }
        if (descrView.required && TextUtils.isEmpty(tvDateTo.text.toString())) {
            return descrView.desc2 ?: ""
        }
        return ""
    }
}