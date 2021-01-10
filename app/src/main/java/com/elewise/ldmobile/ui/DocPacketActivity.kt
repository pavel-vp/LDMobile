package com.elewise.ldmobile.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.elewise.ldmobile.R
import com.elewise.ldmobile.service.Session
import com.google.android.material.tabs.TabLayout

// todo remove this!!!!!
class DocPacketActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc)
        val mSectionsPagerAdapter: SectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, this)
        val viewPager = findViewById<ViewPager>(R.id.vpDoc)
        viewPager.adapter = mSectionsPagerAdapter

        // Передаём ViewPager в TabLayout
        val tabLayout = findViewById<TabLayout>(R.id.tlDocs)
        tabLayout.setupWithViewPager(viewPager)
        updateActionBar(Session.getInstance().currentDocumentDetail.doc_title ?: "")
    }

    private inner class SectionsPagerAdapter(fm: FragmentManager?, context: Context) : FragmentPagerAdapter(fm) {
        private val tabTitles: Array<String>
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                DocPacketFragment.newInstance()
            } else HistoryFragment.newInstance()
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position]
        }

        init {
            tabTitles = arrayOf(context.getString(R.string.doc_activity_tab_document),
                    context.getString(R.string.doc_activity_tab_history))
        }
    }
}