package com.example.vconference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;

import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.ContactList;
import com.example.vconference.custom.objects.VUser;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBSettings;

public class VApp extends Application {
	public static String APP_PATH;
	private VUser user;
	private Map<Integer, VUser> dialogsUsers = new HashMap<Integer, VUser>();
	private Map<String, VUser> dialogsUsersNameMap = new HashMap<String, VUser>();
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
		
		String saveFile = APP_PATH + "/contactList.ser";
		ContactList.getInstance(getApplicationContext(), saveFile);
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
	
	public VUser getUser() {
		return user;
	}

	public void setUser(VUser user) {
		this.user = user;
	}

	public void setDialogsUsers(List<VUser> setUsers) {
        dialogsUsers.clear();
        dialogsUsersNameMap.clear();

        for (VUser user : setUsers) {
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
