package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.FilterElement
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
            val adapter = CustomAdapter(context, rData,
                    R.layout.simple_spinner_item, arrayOf("name"), intArrayOf(android.R.id.text1))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    adapter.selectedItem = position
                }
            }
        }

        descrView.last_value?.let {
            setValue1(it)
        }
    }

    override fun setValue1(data: String) {
        var position = 0
        descrView.list?.forEach { item ->
            if (item.code == data) {
                position = descrView.list.indexOf(item)
                return@forEach
            }
        }
        spinner.setSelection(position)
    }

    override fun getValue1(): String {
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

    class CustomAdapter(val context: Context, data: List<Map<String, String>>, resource: Int, from: Array<String>, to: IntArray): SimpleAdapter(context, data, resource, from, to) {

        var selectedItem = -1;

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = super.getDropDownView(position, null, parent)
            val text1 = v.findViewById<TextView>(android.R.id.text1)

            if (position == selectedItem) {
                v.setBackgroundColor(context.resources.getColor(R.color.spinner_selected))
                text1.setTextColor(context.resources.getColor(android.R.color.black))
            } else {
                v.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
                text1.setTextColor(context.resources.getColor(R.color.gray_dark))
            }

            return v
        }
    }

    override fun setValue2(data: String) = Unit

    override fun getValue2(): String = ""
}