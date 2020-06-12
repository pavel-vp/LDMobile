package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elewise.ldmobile.R
import com.elewise.ldmobile.ui.AddToChainDialogFragment
import kotlinx.android.synthetic.main.cert_widget.view.*

class CertWidgetTrust(context: Context, val name: String, val dialog: AddToChainDialogFragment): LinearLayout(context) {
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