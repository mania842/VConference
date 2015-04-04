package com.example.vconference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;

public class VideoConferenceApplication extends Application {
	public static String APP_PATH;
	private QBUser user;
	private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		init();
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
	
	public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
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

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
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
