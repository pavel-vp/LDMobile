package com.elewise.ldmobile.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.data.DocumentHistory

class HistoryAdapter(private val context: Context, private val histories: List<DocumentHistory>) : BaseAdapter() {
    override fun getCount(): Int {
        return histories.size
    }

    override fun getItem(position: Int): Any {
        return histories.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_hist_item, parent, false)
        val tvHistDate = view.findViewById<TextView>(R.id.tvHistDate)
        tvHistDate.text = histories[position].history_date
        val tvHistEmployee = view.findViewById<TextView>(R.id.tvHistEmployee)
        tvHistEmployee.text = histories[position].employee
        val tvHistText = view.findViewById<TextView>(R.id.tvHistText)
        tvHistText.text = histories[position].text
        return view
    }
}