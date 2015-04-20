package com.example.vconference.ui.adapter;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.ui.ChatActivity;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

@SuppressLint("SimpleDateFormat")
public class ChatAdapter extends BaseAdapter {
	private final List<QBChatMessage> chatMessages;
	private ChatActivity context;
	private VApp app;
	private QBUser currentUser;

	public ChatAdapter(ChatActivity context, List<QBChatMessage> chatMessages) {
		this.app = VApp.getInstance();
		this.context = context;
		this.chatMessages = chatMessages;
		currentUser = app.getUser();
	}

	@Override
	public int getCount() {
		if (chatMessages != null) {
			return chatMessages.size();
		} else {
			return 0;
		}
	}

	@Override
	public QBChatMessage getItem(int position) {
		if (chatMessages != null) {
			return chatMessages.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		QBChatMessage chatMessage = getItem(position);
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = vi.inflate(R.layout.list_item_message, null);
			holder = createViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position > 0) {
			QBChatMessage preItem = getItem(position - 1);
			if (!VApp.isSameDay(preItem, chatMessage)) {
				holder.dateContent.setVisibility(View.VISIBLE);
				holder.txtDate.setText(VApp.getDateText(chatMessage));

			} else {
				holder.dateContent.setVisibility(View.GONE);
			}
		} else {
			holder.dateContent.setVisibility(View.VISIBLE);
			holder.txtDate.setText(VApp.getDateText(chatMessage));
		}

		Map<String, String> chatMap = chatMessage.getProperties();
		if (chatMap.containsKey(ChatActivity.VIDEO)) {
			holder.layout_notification.setVisibility(View.VISIBLE);
			holder.txtNotificationDate.setVisibility(View.VISIBLE);
			holder.txtNotification.setText(chatMap.get(ChatActivity.VIDEO));
			holder.txtNotificationDate.setText(VApp.getTimeText(chatMessage));
//			context.refreshCameraInfo();

			holder.myContent.setVisibility(View.GONE);
			holder.otherContent.setVisibility(View.GONE);
			
		} else if (chatMap.containsKey(ChatActivity.INVITING_USER)) {
			holder.layout_notification.setVisibility(View.VISIBLE);
			holder.txtNotificationDate.setVisibility(View.GONE);
			String notification = app.getUserNameById(Integer.parseInt(chatMap.get(ChatActivity.INVITING_USER))) + " invited ";

			String[] ids = chatMap.get(ChatActivity.INVITED_USERS).split(" ");
			for (int i = 0; i < ids.length; i++) {
				notification += app.getUserNameById(Integer.parseInt(ids[i]));
				if (i < ids.length - 1) {
					notification += ", ";
				}
			}
			holder.txtNotification.setText(notification);
			
			holder.myContent.setVisibility(View.GONE);
			holder.otherContent.setVisibility(View.GONE);

		} else {
			boolean isOutgoing = chatMessage.getSenderId() == null || chatMessage.getSenderId().equals(currentUser.getId());
			holder.layout_notification.setVisibility(View.GONE);

			setMessage(holder, isOutgoing, chatMessage, position);
		}

		return convertView;
	}

	private void setMessage(ViewHolder holder, boolean isOutgoing, QBChatMessage chatMessage, int position) {
		if (isOutgoing) {
			holder.myContent.setVisibility(View.VISIBLE);
			holder.otherContent.setVisibility(View.GONE);

			if (position < getCount() - 1) {
				QBChatMessage nextItem = getItem(position + 1);
				boolean isNextOutgoing = nextItem.getSenderId() == null || nextItem.getSenderId().equals(currentUser.getId());
				boolean isNextVideo = nextItem.getProperties().containsKey(ChatActivity.VIDEO);
				if (VApp.isSameDay(nextItem, chatMessage) && VApp.getTimeText(chatMessage).equals(VApp.getTimeText(nextItem)) && isNextOutgoing && !isNextVideo) {
					holder.myTime.setVisibility(View.GONE);
					holder.view_my_margin.setVisibility(View.GONE);
				} else {
					holder.myTime.setVisibility(View.VISIBLE);
					holder.view_my_margin.setVisibility(View.VISIBLE);
				}
			} else {
				holder.myTime.setVisibility(View.VISIBLE);
				holder.view_my_margin.setVisibility(View.VISIBLE);
			}

			holder.myTime.setText(VApp.getTimeText(chatMessage));
			holder.myMessage.setText(chatMessage.getBody());
		} else {
			holder.otherContent.setVisibility(View.VISIBLE);
			holder.myContent.setVisibility(View.GONE);

			String userName = app.getUserNameById(chatMessage.getSenderId());
			holder.otherUserName.setText(userName);
			holder.otherTime.setText(VApp.getTimeText(chatMessage));
			holder.otherMessage.setText(chatMessage.getBody());
			holder.otherUserName.setVisibility(View.VISIBLE);

			if (position < getCount() - 1) {
				QBChatMessage nextItem = getItem(position + 1);
				boolean isNextOutgoing = nextItem.getSenderId() == null || nextItem.getSenderId().equals(currentUser.getId());
				boolean isNextVideo = nextItem.getProperties().containsKey(ChatActivity.VIDEO);
				if (VApp.isSameDay(nextItem, chatMessage) && VApp.getTimeText(chatMessage).equals(VApp.getTimeText(nextItem)) && !isNextOutgoing
						&& !isNextVideo) {
					holder.otherTime.setVisibility(View.GONE);
					holder.view_other_margin.setVisibility(View.GONE);
				} else {
					holder.otherTime.setVisibility(View.VISIBLE);
					holder.view_other_margin.setVisibility(View.VISIBLE);
				}
			} else {
				holder.otherTime.setVisibility(View.VISIBLE);
				holder.view_other_margin.setVisibility(View.VISIBLE);
			}

			if (position > 0) {
				QBChatMessage preItem = getItem(position - 1);
				boolean isPreOutgoing = preItem.getSenderId() == null || preItem.getSenderId().equals(currentUser.getId());
				boolean isPreVideo = preItem.getProperties().containsKey(ChatActivity.VIDEO);
				if (chatMessage.getSenderId().equals(preItem.getSenderId())) {
					if (VApp.isSameDay(preItem, chatMessage) && VApp.getTimeText(chatMessage).equals(VApp.getTimeText(preItem)) && !isPreOutgoing
							&& !isPreVideo) {
						holder.otherUserName.setVisibility(View.GONE);
					}
				}
			}

		}
	}

	public void add(QBChatMessage message) {
		chatMessages.add(message);
	}

	public void add(List<QBChatMessage> messages) {
		chatMessages.addAll(messages);
	}

	/*
	 * private void setAlignment(ViewHolder holder, boolean isOutgoing) { if (!isOutgoing) {
	 * holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);
	 * 
	 * LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams(); layoutParams.gravity = Gravity.RIGHT;
	 * holder.contentWithBG.setLayoutParams(layoutParams);
	 * 
	 * RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams(); lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
	 * lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); holder.content.setLayoutParams(lp); layoutParams = (LinearLayout.LayoutParams)
	 * holder.txtMessage.getLayoutParams(); layoutParams.gravity = Gravity.RIGHT; holder.txtMessage.setLayoutParams(layoutParams);
	 * 
	 * layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams(); layoutParams.gravity = Gravity.RIGHT;
	 * holder.txtInfo.setLayoutParams(layoutParams); } else { holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);
	 * 
	 * LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams(); layoutParams.gravity = Gravity.LEFT;
	 * holder.contentWithBG.setLayoutParams(layoutParams);
	 * 
	 * RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams(); lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
	 * lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT); holder.content.setLayoutParams(lp); layoutParams = (LinearLayout.LayoutParams)
	 * holder.txtMessage.getLayoutParams(); layoutParams.gravity = Gravity.LEFT; holder.txtMessage.setLayoutParams(layoutParams);
	 * 
	 * layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams(); layoutParams.gravity = Gravity.LEFT;
	 * holder.txtInfo.setLayoutParams(layoutParams); } }
	 */

	private ViewHolder createViewHolder(View v) {
		ViewHolder holder = new ViewHolder();
		holder.dateContent = (RelativeLayout) v.findViewById(R.id.dateContent);
		holder.txtDate = (TextView) v.findViewById(R.id.txtDate);

		holder.myContent = (LinearLayout) v.findViewById(R.id.myContent);
		holder.myTime = (TextView) v.findViewById(R.id.myTime);
		holder.myMessage = (TextView) v.findViewById(R.id.myMessage);

		holder.otherContent = (LinearLayout) v.findViewById(R.id.otherContent);
		holder.otherTime = (TextView) v.findViewById(R.id.otherTime);
		holder.otherMessage = (TextView) v.findViewById(R.id.otherMessage);
		holder.otherUserName = (TextView) v.findViewById(R.id.otherUserName);

		holder.layout_notification = (RelativeLayout) v.findViewById(R.id.layout_notification);
		holder.txtNotification = (TextView) v.findViewById(R.id.notificaton);
		holder.txtNotificationDate = (TextView) v.findViewById(R.id.notificatonDate);

		holder.view_other_margin = (View) v.findViewById(R.id.view_other_margin);
		holder.view_my_margin = (View) v.findViewById(R.id.view_my_margin);
		return holder;
	}

	private static class ViewHolder {
		public RelativeLayout dateContent;
		public TextView txtDate;

		public LinearLayout myContent;
		public TextView myTime;
		public TextView myMessage;

		public LinearLayout otherContent;
		public TextView otherTime;
		public TextView otherMessage;
		public TextView otherUserName;

		public RelativeLayout layout_notification;
		public TextView txtNotification;
		public TextView txtNotificationDate;

		public View view_other_margin;
		public View view_my_margin;
	}
}