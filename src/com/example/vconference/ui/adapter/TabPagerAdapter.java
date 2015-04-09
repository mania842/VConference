package com.example.vconference.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.vconference.ui.ChatRoomActivity;
import com.example.vconference.ui.sample.Android;
import com.example.vconference.ui.sample.Windows;

public class TabPagerAdapter extends FragmentPagerAdapter {

	public TabPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			System.err.println("create new Android");
			// Fragement for Android Tab
			return new Android();
		case 1:
			System.err.println("create new ios");
			// Fragment for Ios Tab
			return new ChatRoomActivity();
		case 2:
			System.err.println("create new Windows");
			// Fragment for Windows Tab
			return new Windows();
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3; // No of Tabs
	}
}