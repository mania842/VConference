package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;

import com.example.vconference.R;
import com.example.vconference.ui.adapter.ContactListAdapter;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class ContactListActivity extends Activity {
	private ListView contactListView;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		contactListView = (ListView) findViewById(R.id.list);
		final ContactListAdapter adapter = new ContactListAdapter(this);
		contactListView.setAdapter(adapter);
		
		QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
		pagedRequestBuilder.setPage(1);
		pagedRequestBuilder.setPerPage(50);
		 
		ArrayList<String> phones = new ArrayList<String>();
		phones.add("917-504-9043");
//		phones.add("7849293423");
		 
		QBUsers.getUsersByPhoneNumbers(phones, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
		    @Override
		    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
//		    	System.out.println(users);
		    }
		 
		    @Override
		    public void onError(List<String> errors) {
		 
		    }
		});
		
//		getContactList();
	}

	private void getContactList() {
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//						System.out.println(name + " : " + phoneNo);
						
						// Toast.makeText(NativeContentProvider.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
					}
					pCur.close();
				}
			}
		}
	}

}
