package com.tasks.medicine.study.reminderapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class OnboardingActivity extends AppCompatActivity implements OnboardingInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ViewPager viewPager = findViewById(R.id.viewPagerOnBoarding);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(OnboardingFragment.newInstance(R.drawable.anywhere_icon, getResources().getString(R.string.flexibility_o_title), getResources().getString(R.string.flexibility_o_text)));
        viewPagerAdapter.addFragment(OnboardingFragment.newInstance(R.drawable.quicker_icon, getResources().getString(R.string.speed_o_title), getResources().getString(R.string.speed_o_text)));

        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayoutIndicator);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void showRemindersActivity(){
        Intent remindersAIntent = new Intent(OnboardingActivity.this, RemindersActivity.class);
        startActivity(remindersAIntent);
        finish();
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            return mList.get(i);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        public void addFragment(Fragment fragment) {
            mList.add(fragment);
        }


    }

}