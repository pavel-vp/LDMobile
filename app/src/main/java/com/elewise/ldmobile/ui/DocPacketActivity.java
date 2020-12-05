package com.elewise.ldmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.service.Session;

public class DocPacketActivity extends BaseActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        viewPager = findViewById(R.id.vpDoc);
        viewPager.setAdapter(mSectionsPagerAdapter);

        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = findViewById(R.id.tlDocs);
        tabLayout.setupWithViewPager(viewPager);

        updateActionBar(Session.getInstance().getCurrentDocumentDetail().getDoc_title());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private String tabTitles[];

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            tabTitles = new String[] { context.getString(R.string.doc_activity_tab_document),
                    context.getString(R.string.doc_activity_tab_history)};
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return DocPacketFragment.newInstance();
                }
                default: {
                    return HistoryFragment.newInstance();
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override public CharSequence getPageTitle(int position) {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position];
        }
    }
}
