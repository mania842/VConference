package com.example.vconference.custom.objects;

import java.io.Serializable;

import com.qb.gson.Gson;
import com.quickblox.users.model.QBUser;

public class VUser extends QBUser implements Serializable {
	private static final long serialVersionUID = -33647284158282101L;
	private UserData userData;

	public VUser() {
		super();
	}
	
	public VUser(QBUser user) {
		this();
		user.copyFieldsTo(this);
	}

	public VUser(Integer id) {
		super(id);
	}

	public VUser(String login, String password, String email) {
		super(login, password, email);
	}

	public VUser(String login, String password) {
		super(login, password);
	}

	public VUser(String login) {
		super(login);
	}

	public void setStatus(String status) {
		if (userData == null)
			userData = new UserData();
		
		userData.setStatus(status);
	}
	
	public String getStatus() {
		if (userData != null)
			return userData.getStatus();
		else if (getCustomData() != null) {
			Gson gson = new Gson();
			userData = gson.fromJson(getCustomData(), UserData.class);
			return userData.getStatus();
		}
		return null;
	}

	public void setCustomData() {
		super.setCustomDataAsObject(userData);
	}

	public class UserData implements Serializable{
		private static final long serialVersionUID = 4032409755963101278L;
		private String status;

		private UserData() {
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}
}
