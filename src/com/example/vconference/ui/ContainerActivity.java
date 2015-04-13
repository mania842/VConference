package com.example.vconference.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.vconference.R;
import com.example.vconference.Settings;
import com.example.vconference.ui.adapter.TabPagerAdapter;

@SuppressLint("InflateParams")
public class ContainerActivity extends FragmentActivity {
	ViewPager tabViewPager;
	TabPagerAdapter tabAdapter;
	ActionBar actionBar;
	Settings settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		settings = Settings.getInstance();
		tabAdapter = new TabPagerAdapter(getSupportFragmentManager());
		tabViewPager = (ViewPager) findViewById(R.id.pager);
		tabViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(position);
				settings.lastTab = position;
				settings.saveSettings();
			}
		});
		tabViewPager.setAdapter(tabAdapter);
		
		actionBar = getActionBar();
		// Enable Tabs on Action Bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {
				tabViewPager.setCurrentItem(tab.getPosition(), true);
			}

			@Override
			public void onTabUnselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {
			}

			@Override
			public void onTabReselected(android.app.ActionBar.Tab tab, android.app.FragmentTransaction ft) {
			}
		};
		
		
		// Add New Tab
		LayoutInflater inflater = LayoutInflater.from(this);
		View tabView1 = inflater.inflate(R.layout.main_tab_view, null);
		ImageView title1 = (ImageView)tabView1.findViewById(R.id.tabIcon);
		title1.setImageResource(R.drawable.ic_user);
		
		View tabView2 = inflater.inflate(R.layout.main_tab_view, null);
		ImageView title2 = (ImageView)tabView2.findViewById(R.id.tabIcon);
		title2.setImageResource(R.drawable.ic_chat);
		
		View tabView3 = inflater.inflate(R.layout.main_tab_view, null);
		ImageView title3 = (ImageView)tabView3.findViewById(R.id.tabIcon);
		title3.setImageResource(R.drawable.ic_settings);
		
		
//		actionBar.addTab(actionBar.newTab().setText("Android").setTabListener(tabListener));
//		actionBar.addTab(actionBar.newTab().setText("ios").setTabListener(tabListener));
//		actionBar.addTab(actionBar.newTab().setText("windows").setTabListener(tabListener));
//		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_user).setTabListener(tabListener));
//		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_chat).setTabListener(tabListener));
//		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_settings).setTabListener(tabListener));
		
		actionBar.addTab(actionBar.newTab().setCustomView(tabView1).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setCustomView(tabView2).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setCustomView(tabView3).setTabListener(tabListener));
		
		tabView1.getLayoutParams().width = 100;
		tabView2.getLayoutParams().width = 100;
		tabView3.getLayoutParams().width = 100;
		
		tabViewPager.setCurrentItem(settings.lastTab);
	}
}
