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

public class ContactList implements Serializable {
	private static final long serialVersionUID = 42L;
	private static ContactList instance;
	private List<Contact> userContactlist;
	private List<Contact> allContactList;

	private static String saveFile;

	public static ContactList getInstance() {
		if (instance == null) {
			instance = new ContactList();
		}
		return instance;
	}

	private ContactList() {
	}

	public List<Contact> getUserContactlist() {
		return userContactlist;
	}

	public void setUserContactlist(List<Contact> userContactlist) {
		this.userContactlist = userContactlist;
	}

	public List<Contact> getAllContactList() {
		return allContactList;
	}

	public void setAllContactList(List<Contact> allContactList) {
		this.allContactList = allContactList;
	}

	// Deserialize settings.ser
	@SuppressWarnings("resource")
	public static ContactList getInstance(Context context, String saveFile) {
		if (instance != null)
			return instance;
		FileInputStream fio;
		try {
			if (ContactList.saveFile == null)
				ContactList.saveFile = saveFile;

			File file = new File(saveFile);
			if (!file.exists()) {
				instance = getInstance();
				instance.save();
				return instance;
			}
			fio = new FileInputStream(saveFile);
			ObjectInputStream ois = new ObjectInputStream(fio);
			Object obj = ois.readObject();
			if (obj instanceof ContactList) {
				instance = (ContactList) obj;
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
				Log.e("save", "contactList.ser made");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "ContactList [userContactlist=" + userContactlist + ", allContactList=" + allContactList + "]";
	}
}
