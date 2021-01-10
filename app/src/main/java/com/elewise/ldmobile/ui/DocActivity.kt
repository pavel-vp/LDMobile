package com.elewise.ldmobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.view.MenuItem
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.elewise.ldmobile.BuildConfig
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Session
import kotlinx.android.synthetic.main.activity_doc.*
import java.io.File


class DocActivity : BaseActivity() {
    private lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc)
        session = Session.getInstance()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateView()
    }


    private fun updateView() {
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        vpDoc.adapter = mSectionsPagerAdapter
        mSectionsPagerAdapter.notifyDataSetChanged()
        // Передаём ViewPager в TabLayout
        tlDocs.setupWithViewPager(vpDoc)
        updateActionBar(session.currentDocumentDetail.doc_title ?: "")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {
        private lateinit var tabTitles: ArrayList<String>
        private val TAB_TITLE_DOCUMENT = getString(R.string.doc_activity_tab_document)
        private val TAB_TITLE_LINES = getString(R.string.doc_activity_tab_lines)
        private val TAB_TITLE_HISTORY = getString(R.string.doc_activity_tab_history)

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getItem(position: Int): Fragment {
            val name = tabTitles[position]
            return if (name == TAB_TITLE_DOCUMENT) {
                DocFragment()
            } else if (name == TAB_TITLE_LINES) {
                ItemsFragment.newInstance()
            } else {
                HistoryFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return tabTitles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position]
        }

        init {
            val listTitles: MutableList<String> = ArrayList()
            with(session.currentDocumentDetail) {
                header_attributes?.let {
                    if (it.size > 0) {
                        listTitles.add(TAB_TITLE_DOCUMENT)
                    }
                    if (lines != null && lines.size > 0) {
                        listTitles.add(TAB_TITLE_LINES)
                    }
                    if (history != null && history.size > 0) {
                        listTitles.add(TAB_TITLE_HISTORY)
                    }
                    tabTitles = listTitles as ArrayList<String>
                } ?: run { tabTitles = ArrayList() }
            }
        }
    }
}