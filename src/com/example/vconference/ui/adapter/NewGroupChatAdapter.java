package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.NewGroupChatActivity;

public class NewGroupChatAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private NewGroupChatActivity activity;
	private VApp app;

	private List<VUser> orgfriendList;
	private List<VUser> friendList;
	private Map<VUser, Boolean> checkMap;
	
	private List<VUser> invitedVUsers;

	// private List<Boolean> checkList;

	// private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	public NewGroupChatAdapter(NewGroupChatActivity activity, List<VUser> friendList) {
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.orgfriendList = friendList;
		this.friendList = friendList;
		this.checkMap = new HashMap<VUser, Boolean>();
		for (VUser vuser : this.orgfriendList) {
			checkMap.put(vuser, Boolean.FALSE);
		}
		this.app = (VApp) this.activity.getApplication();
	}
	
	public List<VUser> getInvitedVUsers() {
		return invitedVUsers;
	}

	public void setInvitedVUsers(List<VUser> invitedVUsers) {
		this.invitedVUsers = invitedVUsers;
	}

	public List<VUser> getOrgFriendList() {
		return orgfriendList;
	}

	public void setOrgFriendList(List<VUser> friendList) {
		this.checkMap.clear();
		this.orgfriendList = friendList;
		this.friendList = friendList;
		for (VUser vuser : this.orgfriendList) {
			checkMap.put(vuser, Boolean.FALSE);
		}
	}

	public void setFriendList(List<VUser> friendList) {
		this.friendList = friendList;
	}
	public boolean isInvited(VUser vUser) {
		if (invitedVUsers != null && invitedVUsers.contains(vUser)) {
			return true;
		} else
			return false;
	}
	public void setCheckItem(VUser vUser, boolean isChecked) {
		if (isChecked) {
			checkMap.put(vUser, Boolean.TRUE);
			activity.addSelectedFriend(vUser);
		} else {
			checkMap.put(vUser, Boolean.FALSE);
			activity.removeSelectedFriend(vUser);
		}
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		// init view
		//
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listview_item_new_group_chat, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			holder.profileIcon = (ImageView) convertView.findViewById(R.id.profileIcon);
			holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
			holder.status = (TextView) convertView.findViewById(R.id.status);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		//
		final VUser vUser = getItem(position);
		holder.contactName.setText(vUser.getFullName());

		if (vUser.getStatus() != null)
			holder.status.setText(vUser.getStatus());
		else
			holder.status.setText(vUser.getLogin());
		holder.status.setVisibility(View.VISIBLE);

		holder.checkBox.setChecked(checkMap.get(vUser));
		
		if (isInvited(vUser)) {
			holder.checkBox.setChecked(true);
			holder.checkBox.setEnabled(false);
			convertView.setBackgroundColor(0xfff0f0f0);
		} else {
			holder.checkBox.setEnabled(true);
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}
		return convertView;
	}
	
	public List<VUser> getSelectedUsers() {
		List<VUser> selectedUsers = new ArrayList<VUser>();
		for (VUser vUser : checkMap.keySet()) {
			if (checkMap.get(vUser).equals(Boolean.TRUE)) {
				selectedUsers.add(vUser);
			}
		}
		return selectedUsers;
	}
	public ArrayList<Integer> getSelectedUserIds() {
		ArrayList<Integer> selectedUserIds = new ArrayList<Integer>();
		for (VUser vUser : checkMap.keySet()) {
			if (checkMap.get(vUser).equals(Boolean.TRUE)) {
				selectedUserIds.add(vUser.getId());
			}
		}
		return selectedUserIds;
	}

	private static class ViewHolder {
		CheckBox checkBox;
		ImageView profileIcon;
		TextView contactName;
		TextView status;
		// TextView groupType;
	}
}
