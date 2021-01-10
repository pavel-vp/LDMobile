package com.elewise.ldmobile.widget

import android.content.Context
import android.widget.LinearLayout

abstract class BaseWidget(context: Context): LinearLayout(context) {
    abstract fun setValue1(data: String)
    abstract fun setValue2(data: String)
    abstract fun getValue1(): String?
    abstract fun getValue2(): String?
    abstract fun getName(): String
    abstract fun validate(): String
}