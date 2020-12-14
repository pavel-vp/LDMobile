package com.elewise.ldmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elewise.ldmobile.R;
import com.elewise.ldmobile.model.ProcessType;
import com.elewise.ldmobile.service.Session;

public class DocsActivity extends BaseActivity {

    DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docs);

        drawerLayout = findViewById(R.id.drawer_layout);

        // Получаем ViewPager и устанавливаем в него адаптер
        ViewPager viewPager = findViewById(R.id.vpDocs);
        viewPager.setAdapter(
                new DocFragmentPagerAdapter(getSupportFragmentManager(), DocsActivity.this));

        // Передаём ViewPager в TabLayout
        TabLayout tabLayout = findViewById(R.id.tlDocs);
        tabLayout.setupWithViewPager(viewPager);

        addClickListener();

        TextView tvLogin = findViewById(R.id.tvLogin);
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText(tvLogin.getText().toString().toUpperCase().substring(0, 1));

        updateActionBar(getString(R.string.docs_activity_title), false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                supportInvalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    private void addClickListener() {
        LinearLayout llDocuments = findViewById(R.id.llDocuments);
        llDocuments.setOnClickListener(view -> {
            drawerLayout.closeDrawers();
        });

        LinearLayout llSettings = findViewById(R.id.llSettings);
        llSettings.setOnClickListener(view -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(this, SettingsActivity.class));
        });

        LinearLayout llAbout = findViewById(R.id.llAbout);
        llAbout.setOnClickListener(view -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(this, AboutActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == android.R.id.home && getSupportFragmentManager().getBackStackEntryCount() > 1
                && getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        } else {
            if(item.getItemId() == R.id.action_filter) {
                Intent intent = new Intent(this, FilterActivity.class);
                startActivity(intent);
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settingsItem = menu.findItem(R.id.action_filter);
        if (Session.getInstance().getFilterData().length > 0) {
            settingsItem.setIcon(getDrawable(R.drawable.ic_filter_active));
        } else {
            settingsItem.setIcon(getDrawable(R.drawable.ic_filter));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public static class DocFragmentPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[];

        public DocFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            tabTitles = new String[] { context.getString(R.string.docs_activity_tab_inbox),
                    context.getString(R.string.docs_activity_tab_outbox)};
        }

        @Override public int getCount() {
            return tabTitles.length;
        }

        @Override public Fragment getItem(int position) {
            return DocsFragment.newInstance(position == 0 ? ProcessType.INBOX : ProcessType.OUTBOX);
        }

        @Override public CharSequence getPageTitle(int position) {
            // генерируем заголовок в зависимости от позиции
            return tabTitles[position];
        }
    }
}
