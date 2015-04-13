package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;

import com.example.vconference.R;
import com.example.vconference.Settings;
import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.ContactList;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.adapter.ContactAdapter;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class FragmentContactList extends Fragment {
	private boolean isFirstLoad = true;
	private ListView contactListView;
	private Switch showSwitch;
	private ContactAdapter adapter;
	private ContactList contactList;
	private Settings settings;
	private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = Settings.getInstance();
		contactList = ContactList.getInstance();

		View layout = inflater.inflate(R.layout.activity_contact_list, container, false);
		contactListView = (ListView) layout.findViewById(R.id.list);
		showSwitch = (Switch) layout.findViewById(R.id.showSwitch);

		showSwitch.setChecked(settings.contactShowUsers);
		showSwitch.setEnabled(false);
		showSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.contactShowUsers = isChecked;
				if (isChecked)
					adapter.setDataSource(contactList.getUserContactlist());
				else
					adapter.setDataSource(contactList.getAllContactList());
				adapter.notifyDataSetChanged();
			}
		});

		if (isFirstLoad) {
			setAdapter();
			new ContackTask().execute();
			isFirstLoad = false;
		} else {
			if (adapter != null)
				contactListView.setAdapter(adapter);
		}
		
		if (contactList.getUserContactlist() != null) {
			showSwitch.setEnabled(true);
		}

		return layout;
	}

	@Override
	public void onDestroyView() {
		settings.saveSettings();
		super.onDestroyView();
	}
	
	class ContackTask extends AsyncTask<Void, Void, Void> {
		List<Contact> allContactList;
		List<Contact> userContactlist;
		Set<String> phoneList;
		Set<String> emailList;
		
		@Override
		protected Void doInBackground(Void... params) {
			allContactList = new ArrayList<Contact>();
			userContactlist = new ArrayList<Contact>();
			contactMap = new HashMap<String, VUser>();

			ContentResolver cr = getActivity().getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

			phoneList = new HashSet<String>();
			emailList = new HashSet<String>();
			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {
					String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);

						List<String> phoneNums = new ArrayList<String>();
						while (pCur.moveToNext()) {
							String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							phoneNo = phoneNo.replace("-", "");
							phoneNums.add(phoneNo);
							// Toast.makeText(NativeContentProvider.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
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

						Contact contact = new Contact(name, phoneNums, emails);
						allContactList.add(contact);
						emailCur.close();

						phoneList.addAll(phoneNums);
						emailList.addAll(emails);
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
			
			final QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
			pagedRequestBuilder.setPage(1);
			pagedRequestBuilder.setPerPage(50);
			QBUsers.getUsersByPhoneNumbers(phoneList, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
				@Override
				public void onSuccess(ArrayList<QBUser> users, Bundle params) {
					for (QBUser user : users) {
						if (!contactMap.containsKey(user.getPhone())) {
							if (!contactMap.containsKey(user.getEmail())) {
								if (!contactMap.containsValue(user)) {

									VUser vuser = new VUser(user);
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
										if (!contactMap.containsValue(user)) {
											VUser vuser = new VUser(user);
											contactMap.put(user.getEmail(), vuser);
											// contactMap.put(user.getEmail(), (VUser) user);
										}
									}
								}
							}

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
							showSwitch.setEnabled(true);
							Collections.sort(userContactlist);
							contactList.setAllContactList(allContactList);
							contactList.setUserContactlist(userContactlist);
							
							setAdapter();
							contactList.save();
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
		}
		
		
		
	}

	private void setAdapter() {
		if (settings.contactShowUsers) {
			if (adapter == null) {
				adapter = new ContactAdapter(contactList.getUserContactlist(), getActivity());
				contactListView.setAdapter(adapter);
			} else {
				adapter.setDataSource(contactList.getUserContactlist());
				adapter.notifyDataSetChanged();
			}
		} else {
			if (adapter == null) {
				adapter = new ContactAdapter(contactList.getAllContactList(), getActivity());
				contactListView.setAdapter(adapter);
			} else {
				adapter.setDataSource(contactList.getAllContactList());
				adapter.notifyDataSetChanged();
			}
		}
		
	}
}
