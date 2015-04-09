package com.example.vconference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;

import com.example.vconference.custom.objects.UserData;
import com.qb.gson.Gson;
import com.quickblox.auth.QBAuth;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;

public class VApp extends Application {
	public static String APP_PATH;
	private QBUser user;
	private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();
	private Map<String, QBUser> dialogsUsersNameMap = new HashMap<String, QBUser>();
	private static VApp instance = null;
	
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
	}
	
	
	public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }
	
	public String getUserNameById(Integer userId) {
		QBUser user = dialogsUsers.get(userId);
		if (user == null) {
			return getString(R.string.unknown);
		} else {
			if (user.getCustomData() != null) {
				Gson gson = new Gson();
				UserData userData = gson.fromJson(user.getCustomData(), UserData.class);
				if (userData.getStatus() != null)
					return userData.getStatus();
			}
			return user.getFullName() == null ? user.getLogin() : user.getFullName();
		}
	}
	
	public String getUserNameByLogin(String userName) {
		QBUser user = dialogsUsersNameMap.get(userName);
		
		if (user == null) {
			return getString(R.string.unknown);
		} else {
			if (user.getCustomData() != null) {
				Gson gson = new Gson();
				UserData userData = gson.fromJson(user.getCustomData(), UserData.class);
				if (userData.getStatus() != null)
					return userData.getStatus();
			}
			return user.getFullName() == null ? user.getLogin() : user.getFullName();
		}
		
	}
	
	public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
            dialogsUsersNameMap.put(user.getLogin(), user);
        }
    }
	
	public QBUser getUser() {
		return user;
	}

	public void setUser(QBUser user) {
		this.user = user;
	}

	public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();
        dialogsUsersNameMap.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
            dialogsUsersNameMap.put(user.getLogin(), user);
        }
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
}
