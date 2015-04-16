package com.example.vconference.custom.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.quickblox.chat.model.QBDialog;

public class ChatRoomList implements Serializable {
	private static final long serialVersionUID = -1988328941745414539L;
	private static ChatRoomList instance;

	private List<QBDialog> groupDialogs;
	private List<QBDialog> publicDialogs;
//	private List<QBDialog> privateDialogs;
//	private Map<Integer, QBDialog> map;
	
	private Map<Integer, VUser> dialogsUsers;
	private Map<String, VUser> dialogsUsersNameMap;

	private static String saveFile;

	public static ChatRoomList getInstance() {
		if (instance == null) {
			instance = new ChatRoomList();
		}
		return instance;
	}

	private ChatRoomList() {
	}

	public List<QBDialog> getGroupDialogs() {
		return groupDialogs;
	}

	public void setGroupDialogs(List<QBDialog> groupDialogs) {
		this.groupDialogs = groupDialogs;
	}

	public List<QBDialog> getPublicDialogs() {
		return publicDialogs;
	}

	public void setPublicDialogs(List<QBDialog> publicDialogs) {
		this.publicDialogs = publicDialogs;
	}

//	public List<QBDialog> getPrivateDialogs() {
//		return privateDialogs;
//	}
//
//	public void setPrivateDialogs(List<QBDialog> privateDialogs) {
//		this.privateDialogs = privateDialogs;
//	}
//
//	public Map<Integer, QBDialog> getMap() {
//		return map;
//	}
//
//	public void setMap(Map<Integer, QBDialog> map) {
//		this.map = map;
//	}

	public Map<Integer, VUser> getDialogsUsers() {
		return dialogsUsers;
	}

	public void setDialogsUsers(Map<Integer, VUser> dialogsUsers) {
		this.dialogsUsers = dialogsUsers;
	}

	public Map<String, VUser> getDialogsUsersNameMap() {
		return dialogsUsersNameMap;
	}

	public void setDialogsUsersNameMap(Map<String, VUser> dialogsUsersNameMap) {
		this.dialogsUsersNameMap = dialogsUsersNameMap;
	}

	// Deserialize chatroomlist.ser
	@SuppressWarnings("resource")
	public static ChatRoomList getInstance(Context context, String saveFile) {
		if (instance != null)
			return instance;
		FileInputStream fio;
		try {
			if (ChatRoomList.saveFile == null)
				ChatRoomList.saveFile = saveFile;

			File file = new File(saveFile);
			if (!file.exists()) {
				instance = getInstance();
				instance.save();
				return instance;
			}
			fio = new FileInputStream(saveFile);
			ObjectInputStream ois = new ObjectInputStream(fio);
			Object obj = ois.readObject();
			if (obj instanceof ChatRoomList) {
				instance = (ChatRoomList) obj;
				return getInstance();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void save() {
		try {
			if (saveFile != null || saveFile.trim().equals("")) {
				FileOutputStream fos = new FileOutputStream(saveFile);

				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(this);
				Log.e("save", saveFile + " made");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@Override
//	public String toString() {
//		return "ChatRoomList [groupDialogs=" + groupDialogs + ", publicDialogs=" + publicDialogs + ", privateDialogs=" + privateDialogs + "]";
//	}

}
