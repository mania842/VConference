package com.example.vconference.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.ui.FragmentSettings.SettingsItems;

public class GridSettingsAdapter extends BaseAdapter {
	private Activity context;
	private LayoutInflater inflater;
	private SettingsItems[] items;
	
	public GridSettingsAdapter(Activity context) {
		super();
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		items = new SettingsItems[] {
			SettingsItems.ACCOUNT, SettingsItems.LOG_OUT
		};
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public SettingsItems getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		// init view
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grid_single, null);
			holder = new ViewHolder();
			holder.grid_image = (ImageView) convertView.findViewById(R.id.grid_image);
			holder.grid_text = (TextView) convertView.findViewById(R.id.grid_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (getItem(position) == SettingsItems.ACCOUNT) {
			holder.grid_text.setText(context.getString(R.string.account));
			holder.grid_image.setImageResource(R.drawable.ic_profile);
			
		} else if (getItem(position) == SettingsItems.LOG_OUT) {
			holder.grid_text.setText(context.getString(R.string.sign_out));
			holder.grid_image.setImageResource(R.drawable.ic_logout);
		}
		convertView.setBackgroundColor(0xffffffff);
		return convertView;
	}

	private static class ViewHolder {
		ImageView grid_image;
		TextView grid_text;
	}
}
