package com.elewise.ldmobile.ui

import android.R.attr.*
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elewise.ldmobile.R


open class BaseActivity: AppCompatActivity() {

    protected fun updateActionBar(title: String) {
        updateActionBar(title, true)
    }

    protected fun updateActionBar(title: String, needRightMargin: Boolean = true) {
        getSupportActionBar()?.let { actionBar ->
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val v = if (needRightMargin) {
                inflater.inflate(R.layout.custom_actionbar_with_margin_right, null)
            } else {
                inflater.inflate(R.layout.custom_actionbar, null)
            }

            val p = ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)

            (v.findViewById<View>(R.id.title) as TextView).setText(title)

            actionBar.setCustomView(v, p)
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}