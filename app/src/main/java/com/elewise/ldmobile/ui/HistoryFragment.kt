package com.elewise.ldmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Session
import kotlinx.android.synthetic.main.fragment_doc_history.view.*

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Session.getInstance().currentDocumentDetail
        val rootView = inflater.inflate(R.layout.fragment_doc_history, container, false)
        rootView.lvHist.adapter = HistoryAdapter(this.context!!, Session.getInstance().currentDocumentDetail.history ?: listOf())
        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}