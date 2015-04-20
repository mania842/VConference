package com.example.vconference.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.vconference.ui.FragmentChatRoom;
import com.example.vconference.ui.FragmentFriends;
import com.example.vconference.ui.FragmentSettings;

public class TabPagerAdapter extends FragmentPagerAdapter {
	private FragmentFriends contactList;
	private FragmentChatRoom chatroom;
	private FragmentSettings settings;
	
	public TabPagerAdapter(FragmentManager fm) {
		super(fm);
		contactList = new FragmentFriends();
		chatroom = new FragmentChatRoom();
		settings = new FragmentSettings();
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			// Fragement for Android Tab
			return contactList;
		case 1:
			// Fragment for Ios Tab
			return chatroom;
		case 2:
			// Fragment for Windows Tab
			return settings;
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3; // No of Tabs
	}
}