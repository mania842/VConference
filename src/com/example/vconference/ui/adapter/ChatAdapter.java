package com.example.vconference.ui.adapter;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.ui.ChatActivity;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

@SuppressLint("SimpleDateFormat")
public class ChatAdapter extends BaseAdapter {
	private static Format DATEFORMAT;
	private static Format TIMEFORMAT;

	private final List<QBChatMessage> chatMessages;
	private ChatActivity context;
	private VApp app;

	public ChatAdapter(ChatActivity context, List<QBChatMessage> chatMessages) {
		this.app = VApp.getInstance();
		this.context = context;
		this.chatMessages = chatMessages;
		DATEFORMAT = DateFormat.getMediumDateFormat(context);
		TIMEFORMAT = DateFormat.getTimeFormat(context);
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
			if (!isSameDay(preItem, chatMessage)) {
				holder.dateContent.setVisibility(View.VISIBLE);
				holder.txtDate.setText(getDateText(chatMessage));
				
			} else {
				holder.dateContent.setVisibility(View.GONE);
			}
		} else {
			holder.dateContent.setVisibility(View.VISIBLE);
			holder.txtDate.setText(getDateText(chatMessage));
		}
		
		QBUser currentUser = app.getUser();
		Map<String, String> chatMap = chatMessage.getProperties();
		if (chatMap.containsKey(ChatActivity.VIDEO)) {
			holder.layout_notification.setVisibility(View.VISIBLE);
			holder.txtNotification.setText(chatMap.get(ChatActivity.VIDEO));
			holder.txtNotificationDate.setText(getTimeText(chatMessage));
			context.refreshCameraInfo();
			
			holder.myContent.setVisibility(View.GONE);
			holder.otherContent.setVisibility(View.GONE);
		} else {
			boolean isOutgoing = chatMessage.getSenderId() == null || chatMessage.getSenderId().equals(currentUser.getId());
			holder.layout_notification.setVisibility(View.GONE);
			
			setMessage(holder, isOutgoing, chatMessage);
		}

		return convertView;
	}

	private void setMessage(ViewHolder holder, boolean isOutgoing, QBChatMessage chatMessage) {
		if (isOutgoing) {
			holder.myContent.setVisibility(View.VISIBLE);
			holder.otherContent.setVisibility(View.GONE);
			
			holder.myTime.setText(getTimeText(chatMessage));
			holder.myMessage.setText(chatMessage.getBody());
		} else {
			holder.otherContent.setVisibility(View.VISIBLE);
			holder.myContent.setVisibility(View.GONE);
			
			String userName = app.getUserNameById(chatMessage.getSenderId());
			holder.otherUserName.setText(userName);
			holder.otherTime.setText(getTimeText(chatMessage));
			holder.otherMessage.setText(chatMessage.getBody());
		}
	}

	public void add(QBChatMessage message) {
		chatMessages.add(message);
	}

	public void add(List<QBChatMessage> messages) {
		chatMessages.addAll(messages);
	}

/*	private void setAlignment(ViewHolder holder, boolean isOutgoing) {
		if (!isOutgoing) {
			holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);

			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
			layoutParams.gravity = Gravity.RIGHT;
			holder.contentWithBG.setLayoutParams(layoutParams);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			holder.content.setLayoutParams(lp);
			layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
			layoutParams.gravity = Gravity.RIGHT;
			holder.txtMessage.setLayoutParams(layoutParams);

			layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
			layoutParams.gravity = Gravity.RIGHT;
			holder.txtInfo.setLayoutParams(layoutParams);
		} else {
			holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);

			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
			layoutParams.gravity = Gravity.LEFT;
			holder.contentWithBG.setLayoutParams(layoutParams);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			holder.content.setLayoutParams(lp);
			layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
			layoutParams.gravity = Gravity.LEFT;
			holder.txtMessage.setLayoutParams(layoutParams);

			layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
			layoutParams.gravity = Gravity.LEFT;
			holder.txtInfo.setLayoutParams(layoutParams);
		}
	}*/

	private ViewHolder createViewHolder(View v) {
		ViewHolder holder = new ViewHolder();
		holder.dateContent = (RelativeLayout) v.findViewById(R.id.dateContent);
		holder.txtDate = (TextView) v.findViewById(R.id.txtDate);
		
		holder.myContent = (RelativeLayout) v.findViewById(R.id.myContent);
		holder.myTime = (TextView) v.findViewById(R.id.myTime);
		holder.myMessage = (TextView) v.findViewById(R.id.myMessage);
		
		holder.otherContent = (RelativeLayout) v.findViewById(R.id.otherContent);
		holder.otherTime = (TextView) v.findViewById(R.id.otherTime);
		holder.otherMessage = (TextView) v.findViewById(R.id.otherMessage);
		holder.otherUserName = (TextView) v.findViewById(R.id.otherUserName);
		
		holder.layout_notification = (RelativeLayout) v.findViewById(R.id.layout_notification);
		holder.txtNotification = (TextView) v.findViewById(R.id.notificaton);
		holder.txtNotificationDate = (TextView) v.findViewById(R.id.notificatonDate);
		return holder;
	}

	public String getDateText(QBChatMessage message) {
		long time = message.getDateSent() * 1000;
		Date date = new Date(time);
		// Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		return DATEFORMAT.format(date);
	}
	public String getTimeText(QBChatMessage message) {
		long time = message.getDateSent() * 1000;
		Date date = new Date(time);
		return TIMEFORMAT.format(date);
	}
	
	public static boolean isSameDay(QBChatMessage message1, QBChatMessage message2) {
		long time1 = message1.getDateSent() * 1000;
		Date date1 = new Date(time1);
		long time2 = message2.getDateSent() * 1000;
		Date date2 = new Date(time2);
		return isSameDay(date1, date2);
		
	}
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2
				.get(Calendar.DAY_OF_YEAR));
	}
	public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

	private static class ViewHolder {
		public RelativeLayout dateContent;
		public TextView txtDate;
		
		public RelativeLayout myContent;
		public TextView myTime;
		public TextView myMessage;
		
		public RelativeLayout otherContent;
		public TextView otherTime;
		public TextView otherMessage;
		public TextView otherUserName;
		
		public RelativeLayout layout_notification;
		public TextView txtNotification;
		public TextView txtNotificationDate;
	}
}