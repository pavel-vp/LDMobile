package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elewise.ldmobile.R
import com.elewise.ldmobile.ui.AddToChainDialogFragment
import com.elewise.ldmobile.ui.RemoveFromChailDialogFragment
import kotlinx.android.synthetic.main.cert_widget.view.*

class CertWidgetChain(context: Context, val name: String, val dialog: RemoveFromChailDialogFragment): LinearLayout(context) {
    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.cert_widget, this)
        tvName.text = name

        llView.setOnLongClickListener {
            dialog.showDialog(name)
            true
        }
    }
}