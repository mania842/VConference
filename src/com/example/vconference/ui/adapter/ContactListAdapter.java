package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;

/**
 * Created by igorkhomenko on 9/12/14.
 */
public class ContactListAdapter extends BaseAdapter {

    private List<VUser> dataSource;
    private LayoutInflater inflater;
    private List<VUser> selected = new ArrayList<VUser>();
    private VApp app;

    public ContactListAdapter(List<VUser> dataSource, Activity activity) {
    	app = (VApp) activity.getApplication();
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(activity);
    }

    public List<VUser> getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog_user, null);
            holder = new ViewHolder();
            holder.login = (TextView) convertView.findViewById(R.id.userLogin);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        VUser user = dataSource.get(position);
        if (user != null) {
            holder.login.setText(app.getUserNameById(user.getId()));
        } else {
        	holder.login.setText("");
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView login;
    }
}
