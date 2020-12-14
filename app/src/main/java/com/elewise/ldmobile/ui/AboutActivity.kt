package com.elewise.ldmobile.ui

import android.os.Bundle
import android.view.MenuItem
import com.elewise.ldmobile.R

class AboutActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        updateActionBar(getString(R.string.activity_about_title))
    }
}