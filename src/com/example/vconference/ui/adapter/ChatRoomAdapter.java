package com.example.vconference.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

public class ChatRoomAdapter extends BaseAdapter {
	private List<QBDialog> dataSource;
	private LayoutInflater inflater;
	private Activity activity;
	private VApp app;
	Map<Integer, VUser> userMap;

	public ChatRoomAdapter(List<QBDialog> dataSource, Activity activity) {
		this.dataSource = dataSource;
		if (dataSource == null)
			dataSource = new ArrayList<QBDialog>();
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.app = (VApp) this.activity.getApplication();
		userMap = app.getDialogsUsers();
	}

	public List<QBDialog> getDataSource() {
		return dataSource;
	}

	@Override
	public int getCount() {
		if (dataSource == null) {
			dataSource = new ArrayList<QBDialog>();
			return 0;
		} else
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
			holder.roomImage = (ImageView) convertView.findViewById(R.id.roomImage);
			holder.numGroupChatUsers = (TextView) convertView.findViewById(R.id.numGroupChatUsers);
			holder.name = (TextView) convertView.findViewById(R.id.roomName);
			holder.lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);
			holder.lastMessageTime = (TextView) convertView.findViewById(R.id.lastMessageTime);
			// holder.groupType = (TextView) convertView.findViewById(R.id.textViewGroupType);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		//
		QBDialog dialog = dataSource.get(position);
		StringBuilder sb = new StringBuilder(dialog.getType().toString());

		if (dialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
			holder.name.setText(getGroupDialogName(dialog));
			holder.roomImage.setImageResource(R.drawable.ic_room);
			holder.numGroupChatUsers.setVisibility(View.GONE);
		} else if (dialog.getType().equals(QBDialogType.GROUP)) {
			holder.roomImage.setImageResource(R.drawable.ic_room);
			holder.numGroupChatUsers.setText(String.valueOf(dialog.getOccupants().size()));
			holder.numGroupChatUsers.setVisibility(View.VISIBLE);
			holder.name.setText(getGroupDialogName(dialog));
			sb.append(": ");
			ArrayList<Integer> userIds = dialog.getOccupants();
			for (int i = 0; i < userIds.size(); i++) {
				sb.append(app.getUserNameById(userIds.get(i)));
				if (i < userIds.size() - 1)
					sb.append(", ");
			}
		} else {
			holder.roomImage.setImageResource(R.drawable.ic_user);
			holder.numGroupChatUsers.setVisibility(View.GONE);
			Integer opponentID = app.getOpponentIDForPrivateDialog(dialog);
			QBUser user = app.getDialogsUsers().get(opponentID);
			if (user != null) {
				holder.name.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
			}
		}

		holder.lastMessage.setText(dialog.getLastMessage());
		if (VApp.isToday(dialog.getLastMessageDateSent())) {
			holder.lastMessageTime.setText(VApp.getTimeText(dialog.getLastMessageDateSent()));
		} else {
			if (dialog.getLastMessage() == null) {
				holder.lastMessageTime.setText(VApp.getDateText(dialog.getCreatedAt()));
			} else
				holder.lastMessageTime.setText(VApp.getDateText(dialog.getLastMessageDateSent()));
		}

		return convertView;
	}

	private String getGroupDialogName(QBDialog dialog) {
		if (dialog.getName().equals(VApp.GROUP_CHAT_NAME_NOT_DEFIEND)) {
			return usersListToChatName(dialog.getOccupants());
		} else {
			return dialog.getName();
		}
	}

	private String usersListToChatName(List<Integer> occupants) {
		List<VUser> vUserList = new ArrayList<VUser>();
		for (Integer id : occupants) {
			VUser vUser = userMap.get(id);
			if (!vUser.equals(app.getUser())) {
				vUserList.add(vUser);
			}
		}

		String chatName = "";
		for (VUser user : vUserList) {
			String prefix = chatName.equals("") ? "" : ", ";
			chatName = chatName + prefix + app.getUserNameById(user.getId());
			// chatName = chatName + prefix + user.getLogin();
		}
		return chatName;
	}

	private static class ViewHolder {
		ImageView roomImage;
		TextView numGroupChatUsers;
		TextView name;
		TextView lastMessage;
		TextView lastMessageTime;
		// TextView groupType;
	}

	public void setDataSource(List<QBDialog> dialogs) {
		this.dataSource = dialogs;
	}
}
