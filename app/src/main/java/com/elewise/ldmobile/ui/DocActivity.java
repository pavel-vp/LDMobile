package com.elewise.ldmobile.ui;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.MenuItem;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.api.ParamDocumentDetailsResponse;
import com.elewise.ldmobile.service.Session;

import java.util.ArrayList;
import java.util.List;

public class DocActivity extends BaseActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager viewPager;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc);

        session = Session.getInstance();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.vpDoc);
        viewPager.setAdapter(mSectionsPagerAdapter);

        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = findViewById(R.id.tlDocs);
        tabLayout.setupWithViewPager(viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateActionBar(session.getCurrentDocumentDetail().getDoc_title());
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

        private String TAB_TITLE_DOCUMENT = getString(R.string.doc_activity_tab_document);
        private String TAB_TITLE_LINES = getString(R.string.doc_activity_tab_lines);
        private String TAB_TITLE_HISTORY = getString(R.string.doc_activity_tab_history);

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            List<String> listTitles = new ArrayList<>();

            ParamDocumentDetailsResponse curentDocument = session.getCurrentDocumentDetail();
            if (curentDocument.getHeader_attributes().length > 0) {
                listTitles.add(TAB_TITLE_DOCUMENT);
            }
            if (curentDocument.getLines() != null && curentDocument.getLines().length > 0) {
                listTitles.add(TAB_TITLE_LINES);
            }

            if (curentDocument.getHistory() != null && curentDocument.getHistory().length > 0) {
                listTitles.add(TAB_TITLE_HISTORY);
            }

            tabTitles = listTitles.toArray(new String[listTitles.size()]);
        }

        @Override
        public Fragment getItem(int position) {

            String name = tabTitles[position];
            if (name.equals(TAB_TITLE_DOCUMENT)) {
                return DocFragment.newInstance();
            } else if (name.equals(TAB_TITLE_LINES)) {
                return ItemsFragment.newInstance();
            } else {
                return HistoryFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override public CharSequence getPageTitle(int position) {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position];
        }
    }
}
