package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.vconference.custom.objects.ChatRoomList;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.adapter.ChatRoomAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

@SuppressLint("UseSparseArrays")
public class FragmentChatRoom extends Fragment {
	private boolean isFirstLoad = true;
	static private boolean isGettingChatRoom;

	private ListView dialogsListView;
	private ProgressBar progressBar;
	private ChatRoomAdapter adapter;

	private VApp app;

	private ChatRoomList chatRoomList;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VApp) getActivity().getApplication();
		chatRoomList = ChatRoomList.getInstance();

		View chatroomView = inflater.inflate(R.layout.activity_dialog, container, false);
		dialogsListView = (ListView) chatroomView.findViewById(R.id.roomsList);
		progressBar = (ProgressBar) chatroomView.findViewById(R.id.progressBar);

		progressBar.setVisibility(View.VISIBLE);
		if (isFirstLoad) {
			setAdapter();
			// new ContactTask().execute();
			isFirstLoad = false;
		} else {
			if (adapter != null)
				dialogsListView.setAdapter(adapter);
		}

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
		refreshChatRoom();
		return chatroomView;
	}

	private void refreshDialogs() {
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
				final ArrayList<QBDialog> groupDialogs = new ArrayList<QBDialog>();
				final ArrayList<QBDialog> privateDialogs = new ArrayList<QBDialog>();
				final Map<Integer, QBDialog> privateDialogsMap = new HashMap<Integer, QBDialog>();

				VUser myUser = app.getUser();
				for (QBDialog dialog : dialogs) {
					usersIDs.addAll(dialog.getOccupants());
					if (dialog.getType() != QBDialogType.PUBLIC_GROUP) {
						groupDialogs.add(dialog);
						if (dialog.getType() == QBDialogType.PRIVATE) {
							for (Integer userId : dialog.getOccupants()) {
								if (!userId.equals(myUser.getId())) {
									privateDialogsMap.put(userId, dialog);
									break;
								}
							}

							privateDialogs.add(dialog);
						}
					} else {
						publicDialogs.add(dialog);
					}
				}
				ChatRoomList.getInstance().setPublicDialogs(publicDialogs);
				ChatRoomList.getInstance().setGroupDialogs(groupDialogs);

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
						ArrayList<VUser> vUsers = new ArrayList<VUser>();
						for (QBUser user : users) {
							vUsers.add(new VUser(user));
						}
						app.setDialogsUsers(vUsers);

						// build list view
						setAdapter();
						isGettingChatRoom = false;
						ChatRoomList.getInstance().save();
					}

					@Override
					public void onError(List<String> errors) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
						dialog.setMessage("get occupants errors: " + errors).create().show();
						isGettingChatRoom = false;
					}

				});
			}

			@Override
			public void onError(List<String> errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				dialog.setMessage("get dialogs errors: " + errors).create().show();
			}
		});
	}

	private void setAdapter() {
		if (adapter == null) {
			adapter = new ChatRoomAdapter(chatRoomList.getGroupDialogs(), getActivity());
			dialogsListView.setAdapter(adapter);
			progressBar.setVisibility(View.GONE);
		} else {
			adapter.setDataSource(chatRoomList.getGroupDialogs());
			adapter.notifyDataSetChanged();
			progressBar.setVisibility(View.GONE);
		}
	}

	public void refreshChatRoom() {
		if (isGettingChatRoom)
			return;
		new ChatRoomTask().execute();
	}

	class ChatRoomTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				if (isGettingChatRoom)
					return;
				isGettingChatRoom = true;
				refreshDialogs();
			} catch (NullPointerException e) {

			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.chatrooms, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.new_chat) {

//			Intent intent = new Intent(getActivity(), NewDialogActivity.class);
			Intent intent = new Intent(getActivity(), NewGroupChatActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
