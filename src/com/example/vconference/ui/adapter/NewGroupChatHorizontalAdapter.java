package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;

public class NewGroupChatHorizontalAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Activity activity;
	private VApp app;

	private List<VUser> friendList;

	// private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	public NewGroupChatHorizontalAdapter(Activity activity) {
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.friendList = new ArrayList<VUser>();
		this.app = (VApp) this.activity.getApplication();
	}

	public List<VUser> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<VUser> friendList) {
		this.friendList = friendList;
	}
	
	public void addFriend(VUser vUser) {
		if (!friendList.contains(vUser))
			friendList.add(0, vUser);
	}
	
	public void removeFriend(VUser vUser) {
		if (friendList.contains(vUser))
			friendList.remove(vUser);
	}

	@Override
	public int getCount() {
		return friendList.size();
	}

	@Override
	public VUser getItem(int position) {
		return friendList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		// init view
		//
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listview_item_horizontal_new_group_chat, null);
			holder = new ViewHolder();
			holder.profileIcon = (ImageView) convertView.findViewById(R.id.profileIcon);
			holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
			// holder.groupType = (TextView) convertView.findViewById(R.id.textViewGroupType);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		//
		VUser vUser = getItem(position);
		holder.contactName.setText(vUser.getFullName());

		return convertView;
	}

	private static class ViewHolder {
		ImageView profileIcon;
		TextView contactName;
		// TextView groupType;
	}
}
