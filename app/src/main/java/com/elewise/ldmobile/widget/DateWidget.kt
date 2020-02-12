package com.elewise.ldmobile.widget

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnClickListener
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
import kotlinx.android.synthetic.main.date_widget.view.*
import java.text.SimpleDateFormat
import java.util.*

class DateWidget(context: Context, val descrView: FilterElement): BaseWidget(context) {

    private val cldr = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd.MM.yyyy")

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.date_widget, this)
        tvDateName.text = descrView.desc
        rlDate.setOnClickListener{ view ->
            try {
                cldr.setTime(sdf.parse(tvDate.text.toString()))
            } catch (e: Exception) {
                Log.e("error parse date", e.toString())
            }

            // date picker dialog
            DatePickerDialog(context,
                    { v, year1, monthOfYear, dayOfMonth ->
                        tvDate.text = (if(dayOfMonth<10) "0"+dayOfMonth.toString() else dayOfMonth.toString()) + "." + (monthOfYear + 1) + "." + year1 },
                    cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH)).show()
        }
        descrView.last_value?.let {
            setData(it)
        }

    }

    override fun setData(data: String) {
        tvDate.text = data
    }

    override fun getData(): String {
        return tvDate.text.toString()
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        return ""
    }
}