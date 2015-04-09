package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.ui.adapter.ChatRoomAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class ChatRoomActivity extends Fragment {
	private ListView dialogsListView;
	private ProgressBar progressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View chatroomView = inflater.inflate(R.layout.activity_dialog, container, false);
		getContactList();
		dialogsListView = (ListView) chatroomView.findViewById(R.id.roomsList);
		progressBar = (ProgressBar) chatroomView.findViewById(R.id.progressBar);

		// get dialogs
		//
		QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
		customObjectRequestBuilder.setPagesLimit(100);
		
		QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
			@Override
			public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {
				// collect all occupants ids
				//
				List<Integer> usersIDs = new ArrayList<Integer>();
				final ArrayList<QBDialog> publicDialogs = new ArrayList<QBDialog>();
				final ArrayList<QBDialog> privateDialogs = new ArrayList<QBDialog>();
				for (QBDialog dialog : dialogs) {
					usersIDs.addAll(dialog.getOccupants());
					if (dialog.getType() != QBDialogType.PUBLIC_GROUP) {
						privateDialogs.add(dialog);
					} else {
						publicDialogs.add(dialog);
					}
				}
				
				// Get all occupants info
				//
				QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
				requestBuilder.setPage(1);
				requestBuilder.setPerPage(usersIDs.size());
				//
				QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
					@Override
					public void onSuccess(ArrayList<QBUser> users, Bundle params) {

						// Save users
						//
						((VApp) getActivity().getApplication()).setDialogsUsers(users);

						// build list view
						//
						buildListView(privateDialogs);
					}

					@Override
					public void onError(List<String> errors) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
						dialog.setMessage("get occupants errors: " + errors).create().show();
					}

				});
			}

			@Override
			public void onError(List<String> errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				dialog.setMessage("get dialogs errors: " + errors).create().show();
			}
		});
		
		return chatroomView;
	}

	void buildListView(List<QBDialog> dialogs) {
		final ChatRoomAdapter adapter = new ChatRoomAdapter(dialogs, getActivity());
		dialogsListView.setAdapter(adapter);

		progressBar.setVisibility(View.GONE);

		// choose dialog
		//
		dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				QBDialog selectedDialog = (QBDialog) adapter.getItem(position);

				Bundle bundle = new Bundle();
				bundle.putSerializable(ChatActivity.EXTRA_DIALOG, (QBDialog) adapter.getItem(position));
				
				
				// public group
				if (selectedDialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
					bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PUBLIC_GROUP);
					
				} else if (selectedDialog.getType().equals(QBDialogType.GROUP)) { // group
					bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.GROUP);

					// private
				} else {
					bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PRIVATE);
				}

				// Open chat activity
				//
				ChatActivity.start(getActivity(), bundle);
			}
		});
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.rooms, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		int id = item.getItemId();
//		if (id == R.id.action_add) {
//
//			// go to New Dialog activity
//			//
//			Intent intent = new Intent(ChatRoomActivity.this, NewDialogActivity.class);
//			startActivity(intent);
//			finish();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	private void getContactList() {
		ContentResolver cr = getActivity().getContentResolver();
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
