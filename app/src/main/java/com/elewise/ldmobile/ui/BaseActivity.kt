package com.elewise.ldmobile.ui

import android.content.Context
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    fun hideKeyboard(view: View) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}