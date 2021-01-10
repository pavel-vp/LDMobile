package com.elewise.ldmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.data.DocumentItem
import com.elewise.ldmobile.service.Session
import kotlinx.android.synthetic.main.doc_line_item.view.*
import kotlinx.android.synthetic.main.fragment_doc_items.view.*

class ItemsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val detail = Session.getInstance().currentDocumentDetail
        val rootView = inflater.inflate(R.layout.fragment_doc_items, container, false)
        if (detail != null && detail.lines != null) {
            for (item in detail.lines) {
                val convertView = inflater.inflate(R.layout.doc_line_item, container, false)
                convertView.tvName.text = item.line_name
                convertView.tvDesc.text = item.line_desc
                rootView.llDynamicPart.addView(convertView)
                convertView.tag = item
                convertView.setOnClickListener { view: View ->
                    val documentItem = view.tag as DocumentItem
                    Session.getInstance().currentDocumentItem = documentItem
                    startActivity(Intent(context, DocLineDetailActivity::class.java))
                }
            }
        }
        return rootView
    }

    companion object {
        fun newInstance(): ItemsFragment {
            return ItemsFragment()
        }
    }
}