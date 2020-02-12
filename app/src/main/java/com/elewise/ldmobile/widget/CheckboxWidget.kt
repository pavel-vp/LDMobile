package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
import kotlinx.android.synthetic.main.checkbox_widget.view.*

class CheckboxWidget(context: Context, val descrView: FilterElement): BaseWidget(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.checkbox_widget, this)
        checkbox.text = descrView.desc
        descrView.last_value?.let {
            setData(it)
        }
    }

    override fun setData(data: String) {
        checkbox.isChecked = data.toBoolean()
    }

    override fun getData(): String {
        return checkbox.isChecked.toString()
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        if (descrView.required && !checkbox.isChecked) {
            return descrView.desc + ", "
        }
        return ""
    }
}