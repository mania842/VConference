package com.example.vconference.ui.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VideoConferenceApplication;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

public class ChatRoomAdapter extends BaseAdapter {
	private List<QBDialog> dataSource;
    private LayoutInflater inflater;
    private Activity activity;
    
    public ChatRoomAdapter (List<QBDialog> dataSource, Activity activity) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(activity);
        this.activity = activity;
    }
    
    public List<QBDialog> getDataSource() {
        return dataSource;
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

        // init view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_item_room, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.roomName);
            holder.lastMessage = (TextView)convertView.findViewById(R.id.lastMessage);
            holder.groupType = (TextView)convertView.findViewById(R.id.textViewGroupType);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        QBDialog dialog = dataSource.get(position);
        if(dialog.getType().equals(QBDialogType.GROUP)){
            holder.name.setText(dialog.getName());
        }else{
            // get opponent name for private dialog
            //
            Integer opponentID = ((VideoConferenceApplication)activity.getApplication()).getOpponentIDForPrivateDialog(dialog);
            QBUser user = ((VideoConferenceApplication)activity.getApplication()).getDialogsUsers().get(opponentID);
            if(user != null){
                holder.name.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
            }
        }

        holder.lastMessage.setText(dialog.getLastMessage());
        holder.groupType.setText(dialog.getType().toString());

        return convertView;
	}
	
	private static class ViewHolder{
        TextView name;
        TextView lastMessage;
        TextView groupType;
    }
}
