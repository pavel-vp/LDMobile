package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
import com.elewise.ldmobile.ui.FilterActivity
import kotlinx.android.synthetic.main.checkbox_widget.view.*

class CheckboxWidget(activity: FilterActivity, val descrView: FilterElement): BaseWidget(activity) {

    init {
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.checkbox_widget, this)
        checkbox.setOnClickListener { activity.hideKeyboard() }
        checkbox.text = descrView.desc
        descrView.last_value?.let {
            setValue1(it)
        }
    }

    override fun setValue1(data: String) {
        checkbox.isChecked = data.toBoolean()
    }

    override fun getValue1(): String {
        return checkbox.isChecked.toString()
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        if (descrView.required && !checkbox.isChecked) {
            return descrView.desc
        }
        return ""
    }

    override fun setValue2(data: String) = Unit

    override fun getValue2(): String = ""
}