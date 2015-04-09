package com.example.vconference.util;

import java.util.HashMap;
import java.util.Map;

import com.example.vconference.VApp;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class ChatUtil {
	public static Map<Integer, QBUser> userMap;

	
	
	public static void addUserToUserMap(QBUser user) {
		if (!userMap.containsKey(user.getId())) {
//			QBUsers.get
			userMap.put(user.getId(), user);
		}
	}
}
