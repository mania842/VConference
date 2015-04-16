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
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.quickblox.customobjects.model.QBCustomObject;

public class FriendList implements Serializable {
	private static final long serialVersionUID = 1847764722474607546L;
	private static FriendList instance;
	private List<VUser> friendList;
	private Integer userId;
	private QBCustomObject qbCustomObject;

	private static String saveFile;

	private static FriendList getInstance() {
		if (instance == null)
			instance = new FriendList();
		return instance;
	}
	public static FriendList getInstance(Integer userId) {
		if (instance == null) {
			instance = new FriendList(userId);
		}
		if (!userId.equals(instance.userId)) {
			instance = new FriendList(userId);
			instance.save();
		}
		return instance;
	}
	
	private FriendList() {
	}
	private FriendList(Integer userId) {
		this.userId = userId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public List<VUser> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<VUser> friendList) {
		this.friendList = friendList;
	}
	
	public QBCustomObject getQbCustomObject() {
		return qbCustomObject;
	}
	public void setQbCustomObject(QBCustomObject qbCustomObject) {
		this.qbCustomObject = qbCustomObject;
	}
	// Deserialize settings.ser
	@SuppressWarnings("resource")
	public static FriendList getInstance(Context context, String saveFile) {
		if (instance != null)
			return instance;
		FileInputStream fio;
		try {
			if (FriendList.saveFile == null)
				FriendList.saveFile = saveFile;

			File file = new File(saveFile);
			if (!file.exists()) {
				instance = getInstance();
				instance.save();
				return instance;
			}
			fio = new FileInputStream(saveFile);
			ObjectInputStream ois = new ObjectInputStream(fio);
			Object obj = ois.readObject();
			if (obj instanceof FriendList) {
				instance = (FriendList) obj;
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

}
