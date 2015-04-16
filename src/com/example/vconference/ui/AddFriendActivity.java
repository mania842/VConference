package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.vconference.R;
import com.example.vconference.Settings;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.ContactList;
import com.example.vconference.custom.objects.FriendList;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.adapter.ContactAdapter;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class AddFriendActivity extends Activity {
	private EditText searchText;
	// private Switch showSwitch;
	private ListView listView;
	private ContactAdapter adapter;
	private ProgressBar progressBar;

	private Settings settings;
	private ContactList contactList;
	private FriendList friendList;
	private VApp app;
	private VUser myUser;
	private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB
	private boolean hasSearchedByLoginOrEmail;
	static private boolean isGettingContactList;

	private ContactTask contactTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_list);

		app = (VApp) getApplication();
		settings = Settings.getInstance();
		myUser = app.getUser();
		contactList = ContactList.getInstance();

		friendList = FriendList.getInstance(myUser.getId());
		searchText = (EditText) findViewById(R.id.searchText);
		listView = (ListView) findViewById(R.id.list);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		if (adapter != null)
			listView.setAdapter(adapter);
		else {
			contactTask = new ContactTask();
			contactTask.execute();
		}
	}

	public void SearchFriend(View view) {
		hasSearchedByLoginOrEmail = true;
		final String searchStr = searchText.getText().toString();
		final ArrayList<Contact> contactList = new ArrayList<Contact>();
		if (searchStr.contains("@") && searchStr.contains(".")) {
			QBUsers.getUserByEmail(searchStr, new QBEntityCallbackImpl<QBUser>() {
				@Override
				public void onSuccess(QBUser user, Bundle args) {
					if (user != null) {
						contactList.add(new Contact(user));
						setAdapter(contactList);
						progressBar.setVisibility(View.GONE);
					}
					QBUsers.getUserByLogin(searchStr, new QBEntityCallbackImpl<QBUser>() {
						@Override
						public void onSuccess(QBUser user, Bundle args) {
							if (user != null) {
								contactList.add(new Contact(user));
								setAdapter(contactList);
								progressBar.setVisibility(View.GONE);
							}
						}

						@Override
						public void onError(List<String> errors) {
							System.out.println(errors);
							progressBar.setVisibility(View.GONE);
						}
					});
				}

				@Override
				public void onError(List<String> errors) {
					System.out.println(errors);
					progressBar.setVisibility(View.GONE);
				}
			});
		} else {
			QBUsers.getUserByLogin(searchStr, new QBEntityCallbackImpl<QBUser>() {
				@Override
				public void onSuccess(QBUser user, Bundle args) {
					if (user != null) {
						contactList.add(new Contact(user));
						setAdapter(contactList);
						progressBar.setVisibility(View.GONE);
					}
				}

				@Override
				public void onError(List<String> errors) {
					System.out.println(errors);
					progressBar.setVisibility(View.GONE);
				}
			});
		}
		// contactTask.cancel(true);
	}

	class ContactTask extends AsyncTask<Void, Void, Void> {
		List<Contact> allContactList;
		List<Contact> userContactlist;
		Set<String> phoneList;
		Set<String> emailList;

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progressBar.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (isGettingContactList)
				return null;
			isGettingContactList = true;
			allContactList = new ArrayList<Contact>();
			userContactlist = new ArrayList<Contact>();
			contactMap = new HashMap<String, VUser>();

			ContentResolver cr = getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

			phoneList = new HashSet<String>();
			emailList = new HashSet<String>();
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { id }, null);

						List<String> phoneNums = new ArrayList<String>();
						while (pCur.moveToNext()) {
							String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							phoneNo = phoneNo.replaceAll("\\D+", "");
							phoneNums.add(phoneNo);

						}
						pCur.close();

						Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
								+ " = ?", new String[] { id }, null);
						List<String> emails = new ArrayList<String>();
						while (emailCur.moveToNext()) {

							String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
							// String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
							if (email != null)
								emails.add(email);

						}
						emailCur.close();

						if (!phoneNums.contains(myUser.getPhone()) || !emails.contains(myUser.getEmail())) {
							Contact contact = new Contact(name, phoneNums, emails);
							allContactList.add(contact);
							phoneList.addAll(phoneNums);
							emailList.addAll(emails);
						}
					}
				}
				cur.close();

				Collections.sort(allContactList);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				final QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
				pagedRequestBuilder.setPage(1);
				pagedRequestBuilder.setPerPage(50);
				QBUsers.getUsersByPhoneNumbers(phoneList, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
					@Override
					public void onSuccess(ArrayList<QBUser> users, Bundle params) {
						for (QBUser user : users) {
							if (!contactMap.containsKey(user.getPhone())) {
								if (!contactMap.containsKey(user.getEmail())) {
									VUser vuser = new VUser(user);
									if (!contactMap.containsValue(vuser)) {
										contactMap.put(user.getPhone(), vuser);
									}
								}
							}
						}

						QBUsers.getUsersByEmails(emailList, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
							@Override
							public void onSuccess(ArrayList<QBUser> users, Bundle params) {
								for (QBUser user : users) {
									if (!contactMap.containsKey(user.getPhone())) {
										if (!contactMap.containsKey(user.getEmail())) {
											VUser vuser = new VUser(user);
											if (!contactMap.containsValue(user)) {
												contactMap.put(user.getEmail(), vuser);
												// contactMap.put(user.getEmail(), (VUser) user);
											}
										}
									}
								}
								if (allContactList == null)
									return;
								for (Contact contact : allContactList) {
									boolean isRegistered = false;
									for (String phone : contact.getPhones()) {
										if (contactMap.containsKey(phone)) {
											isRegistered = true;
											VUser user = contactMap.get(phone);

											contact.setHasId(true);
											contact.setUser(user);
											contact.setStatus(user.getStatus());
											break;
										}
									}

									if (!isRegistered) {
										for (String email : contact.getEmails()) {
											if (contactMap.containsKey(email)) {
												isRegistered = true;
												VUser user = contactMap.get(email);

												contact.setHasId(true);
												contact.setUser(user);
												contact.setStatus(user.getStatus());
												break;
											}
										}
									}

									if (isRegistered) {
										userContactlist.add(contact);
									}
								}
								// adapter.setContactMap(contactMap);
								// showSwitch.setEnabled(true);
								Collections.sort(userContactlist);
								contactList.setAllContactList(allContactList);
								contactList.setUserContactlist(userContactlist);

								if (!hasSearchedByLoginOrEmail && searchText.getText().length() > 0) {
									setAdapter(contactList.getAllContactList());
									hasSearchedByLoginOrEmail = false;
								}
								
								contactList.save();
								isGettingContactList = false;
								progressBar.setVisibility(View.GONE);
							}

							@Override
							public void onError(List<String> errors) {

							}
						});

					}

					@Override
					public void onError(List<String> errors) {

					}
				});
			} catch (NullPointerException e) {

			}
		}

	}

	private void setAdapter(List<Contact> data) {
		// if (settings.contactShowUsers) {
		if (adapter == null) {
			adapter = new ContactAdapter(data, this, friendList);
			listView.setAdapter(adapter);
		} else {
			adapter.setDataSource(data);
			adapter.notifyDataSetChanged();
		}
		// } else {
		// if (adapter == null) {
		// adapter = new ContactAdapter(contactList.getAllContactList(), this);
		// listView.setAdapter(adapter);
		// } else {
		// adapter.setDataSource(contactList.getAllContactList());
		// adapter.notifyDataSetChanged();
		// }
		// }

	}
}
