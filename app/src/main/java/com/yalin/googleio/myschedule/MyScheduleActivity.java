package com.yalin.googleio.myschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.yalin.googleio.Config;
import com.yalin.googleio.R;
import com.yalin.googleio.ui.BaseActivity;
import com.yalin.googleio.util.SettingsUtils;

/**
 * 作者：YaLin
 * 日期：2016/5/6.
 */
public class MyScheduleActivity extends BaseActivity {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private OurViewPagerAdapter mViewPagerAdapter;

    private MyScheduleAdapter mDayZeroAdapter;

    private MyScheduleAdapter[] myScheduleAdapters = new MyScheduleAdapter[Config.CONFERENCE_DAYS.length];

    private int baseTabViewId = 12345;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        if (SettingsUtils.isAttendeeAtVenue(this)) {
            mDayZeroAdapter = new MyScheduleAdapter(this, getLUtils());
            prepareDayZeroAdapter();
        }
        for (int i = 0; i < Config.CONFERENCE_DAYS.length; i++) {
            myScheduleAdapters[i] = new MyScheduleAdapter(this, getLUtils());
        }

        mViewPagerAdapter = new OurViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(
                R.dimen.my_schedule_page_margin));
        mViewPager.setPageMarginDrawable(R.drawable.page_margin);

        overridePendingTransition(0, 0);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_MY_SCHEDULE;
    }

    private void prepareDayZeroAdapter() {

    }

    private class OurViewPagerAdapter extends FragmentPagerAdapter {
        public OurViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
