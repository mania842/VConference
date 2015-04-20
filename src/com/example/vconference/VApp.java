package com.example.vconference;

import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;
import android.text.format.DateFormat;

import com.example.vconference.custom.objects.ChatRoomList;
import com.example.vconference.custom.objects.ContactList;
import com.example.vconference.custom.objects.FriendList;
import com.example.vconference.custom.objects.VUser;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBSettings;

public class VApp extends Application {
	public static String APP_PATH;
	final public static String GROUP_CHAT_NAME_NOT_DEFIEND = "?/NotDefiend/?"; 
	private VUser user;
	private Map<Integer, VUser> dialogsUsers = new HashMap<Integer, VUser>();
	private Map<String, VUser> dialogsUsersNameMap = new HashMap<String, VUser>();
	private static VApp instance = null;
	
	private static Format DATEFORMAT;
	private static Format TIMEFORMAT;
	
	public static VApp getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		createSession();
		init();
	}
	private void createSession() {
		QBSettings.getInstance().fastConfigInit("19109", "NC8QdBBMVxdbmuv", "dMKqMTsV7JvAdMK");
		
		if (!QBChatService.isInitialized()) {
			QBChatService.init(getApplicationContext());
		}
	}
	
	private void init() {
		if (android.os.Build.VERSION.SDK_INT >= 4.2) {
			APP_PATH = getApplicationInfo().dataDir;
		} else {
			APP_PATH = "data/data/" + getPackageName();
		}

		if (APP_PATH == null || APP_PATH.equals("")) {
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return;
		}
		
		String settingFile = APP_PATH + "/settings.ser";
		
		// Deserialize saved settings.ser
		Settings.getInstance(getApplicationContext(), settingFile);
		
		
		String saveFile = APP_PATH + "/friendList.ser";
		FriendList.getInstance(getApplicationContext(), saveFile);
		
		saveFile = APP_PATH + "/contactList.ser";
		ContactList.getInstance(getApplicationContext(), saveFile);
		
		
		
		saveFile = APP_PATH + "/chatroomList.ser";
		ChatRoomList chatRoomList = ChatRoomList.getInstance(getApplicationContext(), saveFile);
		
		this.dialogsUsers = chatRoomList.getDialogsUsers();
		this.dialogsUsersNameMap = chatRoomList.getDialogsUsersNameMap();
		
		
		DATEFORMAT = DateFormat.getMediumDateFormat(getApplicationContext());
		TIMEFORMAT = DateFormat.getTimeFormat(getApplicationContext());
	}
	
	
	public Map<Integer, VUser> getDialogsUsers() {
        return dialogsUsers;
    }
	
	public ArrayList<VUser> getVUsersWithoutMe(ArrayList<Integer> userIds) {
		ArrayList<VUser> vUsers = new ArrayList<VUser>();
		
		for (Integer userId : userIds) {
			if (!userId.equals(user.getId())) {
				VUser user = dialogsUsers.get(userId);
				vUsers.add(user);
			}
		}
		return vUsers;
	}
	
	public String getUserNameById(Integer userId) {
		VUser user = dialogsUsers.get(userId);
		if (user == null) {
			return getString(R.string.unknown);
		} else {
			return user.getStatus() != null ? user.getStatus() : user.getFullName() == null ? user.getLogin() : user.getFullName();
		}
	}
	
	public String getUserNameByLogin(String userName) {
		VUser user = dialogsUsersNameMap.get(userName);
		
		if (user == null) {
			return getString(R.string.unknown);
		} else {
			return user.getStatus() != null ? user.getStatus() : user.getFullName() == null ? user.getLogin() : user.getFullName();
		}
		
	}
	
	public void addDialogsUsers(List<VUser> newUsers) {
        for (VUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
            dialogsUsersNameMap.put(user.getLogin(), user);
        }
    }
	
	public void addDialogsUsers(VUser newUser) {
		dialogsUsers.put(newUser.getId(), newUser);
		dialogsUsersNameMap.put(newUser.getLogin(), newUser);
    }
	
	public VUser getUser() {
		return user;
	}

	public void setUser(VUser user) {
		this.user = user;
	}
	
	public void setDialogsUsers(List<VUser> setUsers) {
		if (dialogsUsers == null)
			dialogsUsers = new HashMap<Integer, VUser>();
		if (dialogsUsersNameMap == null)
			dialogsUsersNameMap = new HashMap<String, VUser>();
		
        dialogsUsers.clear();
        dialogsUsersNameMap.clear();

        for (VUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
            dialogsUsersNameMap.put(user.getLogin(), user);
        }
        
        ChatRoomList.getInstance().setDialogsUsers(dialogsUsers);
        ChatRoomList.getInstance().setDialogsUsersNameMap(dialogsUsersNameMap);
    }

	public Integer getOpponentIDForPrivateDialog(QBDialog dialog) {
		Integer opponentID = -1;
        for(Integer userID : dialog.getOccupants()){
            if(!userID.equals(user.getId())){
                opponentID = userID;
                break;
            }
        }
        return opponentID;
	}
	
	static public String getDateText(QBChatMessage message) {
		long time = message.getDateSent() * 1000;
		Date date = new Date(time);
		// Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		return DATEFORMAT.format(date);
	}
	static public String getDateText(Date date) {
		return DATEFORMAT.format(date);
	}
	static public String getDateText(long time) {
		time *= 1000;
		Date date = new Date(time);
		return DATEFORMAT.format(date);
	}
	static public String getTimeText(QBChatMessage message) {
		long time = message.getDateSent() * 1000;
		Date date = new Date(time);
		return TIMEFORMAT.format(date);
	}
	static public String getTimeText(long time) {
		time *= 1000;
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
	public static boolean isToday(long time) {
		time *= 1000;
		Date date = new Date(time);
        return isSameDay(date, Calendar.getInstance().getTime());
    }
}
