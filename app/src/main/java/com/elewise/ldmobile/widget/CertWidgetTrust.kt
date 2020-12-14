package com.elewise.ldmobile.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.elewise.ldmobile.R
import com.elewise.ldmobile.model.CertificateInfo
import kotlinx.android.synthetic.main.cert_widget.view.*

class CertWidgetTrust(context: Context, val certicateInfo: CertificateInfo, f: (String) -> Unit): LinearLayout(context) {
    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.cert_widget, this)
        tvInfo.text = certicateInfo.info

        llView.setOnLongClickListener {
            f(certicateInfo.alias)
            true
        }
    }
}