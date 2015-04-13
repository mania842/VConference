package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.VUser;
import com.quickblox.users.model.QBUser;

public class ContactAdapter extends BaseAdapter {
	private List<Contact> dataSource;
	private LayoutInflater inflater;
	private Activity activity;
	private VApp app;
//	private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	public ContactAdapter(List<Contact> dataSource, Activity activity) {
		this.dataSource = dataSource;
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.app = (VApp) this.activity.getApplication();
	}

//	public void setContactMap(HashMap<String, VUser> contactMap) {
//		this.contactMap = contactMap;
//
//	}
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
	public Object getItem(int position) {
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
			// holder.groupType = (TextView) convertView.findViewById(R.id.textViewGroupType);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		//
		Contact contact = dataSource.get(position);
		holder.contactName.setText(contact.getName());

		if (contact.isHasId()) {
			if (contact.getStatus() != null)
				holder.status.setText(contact.getStatus());
			else
				holder.status.setText(contact.getUser().getLogin());
			holder.status.setVisibility(View.VISIBLE);
		} else {
			holder.status.setVisibility(View.GONE);
		}
		// holder.lastMessage.setText(contact.get);

		return convertView;
	}

	private static class ViewHolder {
		ImageView profileIcon;
		TextView contactName;
		TextView status;
		// TextView groupType;
	}
}
