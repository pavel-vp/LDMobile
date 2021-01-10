package com.elewise.ldmobile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.elewise.ldmobile.R
import com.elewise.ldmobile.api.ParamExecOperationActionType
import com.elewise.ldmobile.api.ResponseStatusType
import com.elewise.ldmobile.model.ProcessType
import com.elewise.ldmobile.service.Prefs
import com.elewise.ldmobile.service.Session
import com.elewise.ldmobile.ui.DocsFragment.Companion.newInstance
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.drawermenu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DocsActivity : BaseActivity() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private val session = Session.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Получаем ViewPager и устанавливаем в него адаптер
        val viewPager = findViewById<ViewPager>(R.id.vpDocs)
        viewPager.adapter = DocFragmentPagerAdapter(supportFragmentManager, this@DocsActivity)

        // Передаём ViewPager в TabLayout
        val tabLayout = findViewById<TabLayout>(R.id.tlDocs)
        tabLayout.setupWithViewPager(viewPager)
        addClickListener()
        tvLogin.setText(Prefs.getLastLogin(this))
        tvRole.setText(session.lastAuth?.role ?: "")
        tvAvatar.text = tvLogin.text.toString().toUpperCase().substring(0, 1)
        updateActionBar(getString(R.string.docs_activity_title), false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, 0, 0) {
            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                supportInvalidateOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                supportInvalidateOptionsMenu()
            }
        }

        llExit.setOnClickListener {
            uiScope.launch {
                try {
                    val response = session.execOperation(ParamExecOperationActionType.close_session.name).await().body()
                    response?.let {
                        if (it.status == (ResponseStatusType.S.name)) {
                            session.lastAuth = null
                            finish()
                        } else {
                            Toast.makeText(this@DocsActivity, getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@DocsActivity, getString(R.string.error_unknown), Toast.LENGTH_LONG).show()
                }
            }
        }
        mDrawerToggle.syncState()
        drawerLayout.setDrawerListener(mDrawerToggle)
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }

    private fun addClickListener() {
        val llDocuments = findViewById<LinearLayout>(R.id.llDocuments)
        llDocuments.setOnClickListener { view: View? -> drawerLayout.closeDrawers() }
        val llSettings = findViewById<LinearLayout>(R.id.llSettings)
        llSettings.setOnClickListener { view: View? ->
            drawerLayout.closeDrawers()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        val llAbout = findViewById<LinearLayout>(R.id.llAbout)
        llAbout.setOnClickListener { view: View? ->
            drawerLayout.closeDrawers()
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_filter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (mDrawerToggle.isDrawerIndicatorEnabled && mDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else if (item.itemId == android.R.id.home && supportFragmentManager.backStackEntryCount > 1 && supportFragmentManager.popBackStackImmediate()) {
            true
        } else {
            if (item.itemId == R.id.action_filter) {
                val intent = Intent(this, FilterActivity::class.java)
                startActivity(intent)
                true
            } else {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelJob.cancel()
    }

    class DocFragmentPagerAdapter(fm: FragmentManager?, context: Context) : FragmentPagerAdapter(fm) {
        private val tabTitles: Array<String>
        override fun getCount(): Int {
            return tabTitles.size
        }

        override fun getItem(position: Int): Fragment {
            return newInstance(if (position == 0) ProcessType.INBOX else ProcessType.OUTBOX)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position]
        }

        init {
            tabTitles = arrayOf(context.getString(R.string.docs_activity_tab_inbox),
                    context.getString(R.string.docs_activity_tab_outbox))
        }
    }
}