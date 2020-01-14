package com.elewise.ldmobile.widget

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.SimpleAdapter
import com.elewise.ldmobile.R
import com.elewise.ldmobile.model.FilterElement
import kotlinx.android.synthetic.main.select_widget.view.*

class SelectWidget(context: Context, val descrView: FilterElement): BaseWidget(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.select_widget, this)
        tvName.text = descrView.desc
        descrView.list?.let {item ->
            val rData = ArrayList<HashMap<String, String>>()

            item.forEach {
                rData.add(hashMapOf("id" to it.code, "name" to it.desc))
            }
            val adapter = SimpleAdapter(context, rData,
                    android.R.layout.simple_spinner_item, arrayOf("name"), intArrayOf(android.R.id.text1))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        descrView.last_value?.let {
            setData(it)
        }
    }

    override fun setData(data: String) {
        var position = 0
        descrView.list?.forEach { item ->
            if (item.code == data) {
                position = descrView.list.indexOf(item)
                return@forEach
            }
        }
        spinner.setSelection(position)
    }

    override fun getData(): String {
        val selectedItem = spinner.adapter.getItem(spinner.selectedItemPosition) as Map<String, String>
        val id = selectedItem.get("id").toString()
        return id
    }

    override fun getName(): String {
        return descrView.name
    }

    override fun validate(): String {
        return ""
    }
}