package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.vconference.R;
import com.example.vconference.Settings;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.Contact;
import com.example.vconference.custom.objects.FriendList;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.adapter.FriendsListAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class FragmentFriends extends Fragment {
	private boolean isFirstLoad = true;
	private VApp app;
	private VUser myUser;
	private ListView friendListView;
	private Switch showSwitch;
	private FriendsListAdapter adapter;
	private FriendList friendList;
	private Settings settings;
	private HashMap<String, VUser> contactMap; // can check if the contact is registered in DB

	static private boolean isGettingFriendList;

	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = Settings.getInstance();
		app = ((VApp) getActivity().getApplication());
		myUser = app.getUser();
		friendList = FriendList.getInstance(myUser.getId());
		View layout = inflater.inflate(R.layout.activity_contact_list, container, false);
		friendListView = (ListView) layout.findViewById(R.id.list);
		showSwitch = (Switch) layout.findViewById(R.id.showSwitch);
		showSwitch.setVisibility(View.GONE);

		if (isFirstLoad) {
			setAdapter();
			new GetFriendList().execute();
			isFirstLoad = false;
		} else {
			if (adapter != null) {
				adapter.setDataSource(friendList.getFriendList());
				friendListView.setAdapter(adapter);
			}

		}

		friendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				VUser vUser = adapter.getItem(position);
				joinToSingleDialog(vUser);
			}
		});
		return layout;
	}

	public void joinToSingleDialog(VUser user) {
		app.addDialogsUsers(user);

		// Create new group dialog
		//
		QBDialog dialogToCreate = new QBDialog();
		// dialogToCreate.setName(usersListToChatName());
		dialogToCreate.setType(QBDialogType.PRIVATE);
		final ArrayList<Integer> occupantsIds = new ArrayList<Integer>();
		// VUser myUser = app.getUser();
		// occupantsIds.add(myUser.getId());
		occupantsIds.add(user.getId());
		dialogToCreate.setOccupantsIds(occupantsIds);

		QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
			@Override
			public void onSuccess(QBDialog dialog, Bundle args) {
				startSingleChat(dialog);
			}

			@Override
			public void onError(List<String> errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				dialog.setMessage("dialog creation errors: " + errors + " " + occupantsIds).create().show();
			}
		});
	}

	public void startSingleChat(QBDialog dialog) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PRIVATE);
		bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

		ChatActivity.start(getActivity(), bundle);
	}

	@Override
	public void onDestroyView() {
		settings.saveSettings();
		super.onDestroyView();
	}

	public void refreshFriendList() {
		new GetFriendList().execute();
	}

	class GetFriendList extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
			requestBuilder.eq("userId", myUser.getId());
			requestBuilder.setPagesLimit(5);

			QBCustomObjects.getObjects("MyContactList", requestBuilder, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
				@Override
				public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
					if (customObjects.size() == 0) {
						createNewMyContactList();
						return;
					}
					QBCustomObject obj = customObjects.get(0);
					HashMap<String, Object> fields = obj.getFields();
					friendList.setQbCustomObject(obj);
					ArrayList<Integer> friendIds = (ArrayList<Integer>) fields.get("myContacts");
					QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
					pagedRequestBuilder.setPage(1);
					pagedRequestBuilder.setPerPage(50);

					QBUsers.getUsersByIDs(friendIds, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
						@Override
						public void onSuccess(ArrayList<QBUser> users, Bundle params) {
							List<VUser> vUsers = new ArrayList<VUser>();
							for (QBUser user : users) {
								vUsers.add(new VUser(user));
							}
							friendList.setFriendList(vUsers);
							friendList.save();

							adapter.setDataSource(friendList.getFriendList());
							adapter.notifyDataSetChanged();
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

		private void createNewMyContactList() {
			QBCustomObject newRecord = new QBCustomObject();

			// put fields
			List<Integer> myContacts = new ArrayList<Integer>();
			newRecord.putArray("myContacts", myContacts);
			newRecord.putInteger("userId", myUser.getId());

			// set the class name
			newRecord.setClassName("MyContactList");

			QBCustomObjects.createObject(newRecord, new QBEntityCallbackImpl<QBCustomObject>() {
				@Override
				public void onSuccess(QBCustomObject createdObject, Bundle params) {
				}

				@Override
				public void onError(List<String> errors) {
					System.out.println(errors);
				}
			});
		}

	}

	private void setAdapter() {
		if (adapter == null) {
			adapter = new FriendsListAdapter(friendList.getFriendList(), getActivity());
			friendListView.setAdapter(adapter);
		} else {
			adapter.setDataSource(friendList.getFriendList());
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.friendlists, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.add_friend) {
			Intent intent = new Intent(getActivity(), AddFriendActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
