package com.example.vconference.ui.adapter;

import com.example.vconference.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	
	public ContactListAdapter(Context ctx) {
		this.inflater = LayoutInflater.from(ctx);
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.login = (TextView) convertView.findViewById(R.id.userLogin);
            holder.imgView_avatar = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.image);
//        RoundImage roundedImage = new RoundImage(bm);
//        imageView1.setImageDrawable(roundedImage);
        
		return convertView;
	}
	
	private static class ViewHolder {
        TextView login;
        ImageView imgView_avatar;
    }

}
