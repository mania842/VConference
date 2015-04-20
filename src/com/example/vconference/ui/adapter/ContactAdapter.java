package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.FriendList;
import com.example.vconference.custom.objects.VUser;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.model.QBUser;

public class ContactAdapter extends BaseAdapter {
	private List<Contact> dataSource;
	private LayoutInflater inflater;
	private Activity activity;
	private VApp app;

	private QBCustomObject qbCustomObject;
	private List<VUser> friendList;

	// private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	public ContactAdapter(List<Contact> dataSource, Activity activity, FriendList friendList) {
		this.dataSource = dataSource;
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.friendList = friendList.getFriendList();
		this.qbCustomObject = friendList.getQbCustomObject();
		this.app = (VApp) this.activity.getApplication();
	}

	// public void setContactMap(HashMap<String, VUser> contactMap) {
	// this.contactMap = contactMap;
	//
	// }
	public void setDataSource(List<Contact> dataSource) {
		this.dataSource = dataSource;
	}

	public List<Contact> getDataSource() {
		return dataSource;
	}

	@Override
	public int getCount() {
		if (dataSource == null)
			dataSource = new ArrayList<Contact>();
		return dataSource.size();
	}

	@Override
	public Contact getItem(int position) {
		return dataSource.get(position);
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
			convertView = inflater.inflate(R.layout.listview_item_contact, null);
			holder = new ViewHolder();
			holder.profileIcon = (ImageView) convertView.findViewById(R.id.profileIcon);
			holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
			holder.status = (TextView) convertView.findViewById(R.id.status);
			holder.addFriend = (ImageButton) convertView.findViewById(R.id.addFriend);
			// holder.groupType = (TextView) convertView.findViewById(R.id.textViewGroupType);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		//
		final Contact contact = dataSource.get(position);
		holder.contactName.setText(contact.getName());

		if (contact.isHasId()) {
			if (contact.getStatus() != null)
				holder.status.setText(contact.getStatus());
			else
				holder.status.setText(contact.getUser().getLogin());
			holder.status.setVisibility(View.VISIBLE);

			if (friendList == null) {
				holder.addFriend.setVisibility(View.VISIBLE);
			} else {
				if (friendList.contains(contact.getUser())) {
					holder.addFriend.setVisibility(View.GONE);
				} else {
					holder.addFriend.setVisibility(View.VISIBLE);
				}
			}
		} else {
			holder.status.setVisibility(View.GONE);
			holder.addFriend.setVisibility(View.GONE);
		}

		holder.addFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contact.getUser() != null) {
					final QBCustomObject record = new QBCustomObject();
					ArrayList<Integer> friends = new ArrayList<Integer>();
					for (VUser vUser : friendList) {
						friends.add(vUser.getId());
					}

					VUser vUser = contact.getUser();
					friends.add(vUser.getId());
					friendList.add(vUser);

					record.setClassName(qbCustomObject.getClassName());
					record.setCustomObjectId(qbCustomObject.getCustomObjectId());
					record.putArray("myContacts", friends);
					QBCustomObjects.updateObject(record, new QBEntityCallbackImpl<QBCustomObject>() {
						@Override
						public void onSuccess(QBCustomObject result, Bundle params) {
							super.onSuccess(result, params);
							notifyDataSetChanged();
						}

						@Override
						public void onError(List<String> errors) {
							super.onError(errors);
							System.out.println(errors);
						}
					});
				}
				System.out.println("clicked " + contact);
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		ImageView profileIcon;
		TextView contactName;
		TextView status;
		ImageButton addFriend;
		// TextView groupType;
	}
}
