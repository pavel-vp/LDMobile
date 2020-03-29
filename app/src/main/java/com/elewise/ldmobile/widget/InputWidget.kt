package com.elewise.ldmobile.widget

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
import kotlinx.android.synthetic.main.input_widget.view.*

class InputWidget(context: Context, val descrView: FilterElement): BaseWidget(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.input_widget, this)
        tvName.text = descrView.desc
        descrView.last_value?.let {
            setValue1(it)
        }
    }

    override fun setValue1(data: String) {
        edInput.setText(data)
    }

    override fun getValue1(): String {
        return edInput.text.toString()
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        if (descrView.required && TextUtils.isEmpty(edInput.text.toString())) {
            return descrView.desc
        }
        return ""
    }

    override fun setValue2(data: String) = Unit

    override fun getValue2(): String = ""
}