package com.elewise.ldmobile.ui;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.ActionType;

public class DocsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Получаем ViewPager и устанавливаем в него адаптер
        ViewPager viewPager = findViewById(R.id.vpDocs);
        viewPager.setAdapter(
                new DocFragmentPagerAdapter(getSupportFragmentManager(), DocsActivity.this));

        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = findViewById(R.id.tlDocs);
        tabLayout.setupWithViewPager(viewPager);

    }


    public static class DocFragmentPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { "Согласование", "Подписание"};
        private Context context;

        public DocFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override public int getCount() {
            return tabTitles.length;
        }

        @Override public Fragment getItem(int position) {
            return DocsFragment.newInstance(position == 0 ? ActionType.APPROVE : ActionType.SIGNATURE);
        }

        @Override public CharSequence getPageTitle(int position) {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position];
        }
    }
}
