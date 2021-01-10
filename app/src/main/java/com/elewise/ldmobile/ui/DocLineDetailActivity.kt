package com.elewise.ldmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Session
import kotlinx.android.synthetic.main.activity_doc_line_detail.*
import kotlinx.android.synthetic.main.doc_line_detail_item.view.*

class DocLineDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_line_detail)

        with (Session.getInstance().currentDocumentItem) {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            for (item in details) {
                val convertView = inflater.inflate(R.layout.doc_line_detail_item, llDynamicPart, false)
                convertView.tvDesc.text = item.desc
                convertView.tvValue.text = item.value
                llDynamicPart.addView(convertView)
            }
            updateActionBar(line_name)
        }
    }
}