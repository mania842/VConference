package com.example.vconference.custom.objects;

import java.io.Serializable;
import java.util.List;

import com.quickblox.users.model.QBUser;

public class Contact implements Comparable<Contact>, Serializable {
	private static final long serialVersionUID = 3038766855273040642L;
	private String name;
	private List<String> phones;
	private List<String> emails;
	private String status;
	private boolean hasId;
	private VUser user;

	public Contact(QBUser user) {
		this(new VUser(user));
	}
	public Contact(VUser user) {
		super();
		this.user = user;
		this.name = user.getFullName();
		this.status = user.getStatus();
		this.hasId = true;
	}

	public Contact(String name, List<String> phones, List<String> emails) {
		super();
		this.name = name;
		this.phones = phones;
		this.emails = emails;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isHasId() {
		return hasId;
	}

	public void setHasId(boolean hasId) {
		this.hasId = hasId;
	}

	public VUser getUser() {
		return user;
	}

	public void setUser(VUser user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Contact [name=" + name + ", phones=" + phones + ", emails=" + emails + ", status=" + status + ", hasId=" + hasId + ", user=" + user + "]";
	}

	@Override
	public int compareTo(Contact another) {
		if (status != null) {
			if (another.status != null) {
				if (status.compareTo(another.status) != 0) {
					return status.compareTo(another.status);
				} else {
					if (name.compareTo(another.name) != 0)
						return name.compareTo(another.name);
					else
						return 1;
				}
			} else {
				return -1;
			}
		} else {
			if (name.compareTo(another.name) != 0)
				return name.compareTo(another.name);
			else
				return 1;
		}
	}

}
