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

public class FriendsListAdapter extends BaseAdapter {
	private List<VUser> dataSource;
	private LayoutInflater inflater;
	private Activity activity;
	private VApp app;

	public FriendsListAdapter(List<VUser> dataSource, Activity activity) {
		this.dataSource = dataSource;
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.app = (VApp) this.activity.getApplication();
	}

	public void setDataSource(List<VUser> dataSource) {
		this.dataSource = dataSource;
	}
	public List<VUser> getDataSource() {
		return dataSource;
	}

	@Override
	public int getCount() {
		if (dataSource == null)
			dataSource = new ArrayList<VUser>();
		return dataSource.size();
	}

	@Override
	public VUser getItem(int position) {
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
		VUser vUser = dataSource.get(position);
		
		holder.contactName.setText(vUser.getFullName());
		if (vUser.getStatus() == null || vUser.getStatus().trim().length() == 0)
			holder.status.setVisibility(View.GONE);
		else {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setText(vUser.getStatus());
		}

		return convertView;
	}

	private static class ViewHolder {
		ImageView profileIcon;
		TextView contactName;
		TextView status;
		// TextView groupType;
	}
}
