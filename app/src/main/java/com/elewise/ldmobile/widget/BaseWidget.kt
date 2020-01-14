package com.elewise.ldmobile.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

abstract class BaseWidget(context: Context): LinearLayout(context) {
    abstract fun setData(data: String)
    abstract fun getData(): String
    abstract fun getName(): String
    abstract fun validate(): String
}