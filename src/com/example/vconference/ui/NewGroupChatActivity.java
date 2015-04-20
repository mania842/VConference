package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.FriendList;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.custom.view.HorizontalListView;
import com.example.vconference.ui.adapter.NewGroupChatAdapter;
import com.example.vconference.ui.adapter.NewGroupChatHorizontalAdapter;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestUpdateBuilder;

public class NewGroupChatActivity extends Activity {
	final public static String MODE_INVITE = "MODE_INVITE";
	final public static String INVITE_OCCUPANTS = "INVITE_OCCUPANTS";
	final public static String INVITING_DIALOG = "INVITING_DIALOG";
	final public static String CREATING_NEW_GROUP = "CREATING_NEW_GROUP";
	final public static String INVITED_USER_IDS = "INVITED_USER_IDS";
	final public static int REQUEST_CODE = 101;
	private EditText searchText;

	private HorizontalListView selectedListView;
	private ListView listView;
	private NewGroupChatAdapter adapter;
	private NewGroupChatHorizontalAdapter horizontalAdapter;

	private Button btnCreate;
	private ProgressBar progressBar;

	private FriendList friendList;
	private VApp app;
	private VUser myUser;

	private boolean isInvitation;
	private ArrayList<Integer> invitedUsers;
	private QBDialog invitingDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group_chat);
		Intent intent = getIntent();
		isInvitation = intent.getBooleanExtra(MODE_INVITE, false);

		app = (VApp) getApplication();
		myUser = app.getUser();
		getActionBar().setDisplayHomeAsUpEnabled(true);

		friendList = FriendList.getInstance(myUser.getId());
		btnCreate = (Button) findViewById(R.id.btn_create);

		if (isInvitation) {
			btnCreate.setText(getString(R.string.invite));
			invitedUsers = intent.getIntegerArrayListExtra(INVITE_OCCUPANTS);
			invitingDialog = (QBDialog) intent.getSerializableExtra(INVITING_DIALOG);
		} else {
			btnCreate.setText(getString(R.string.create_chatroom));
		}
		selectedListView = (HorizontalListView) findViewById(R.id.selectedListView);
		selectedListView.setVisibility(View.GONE);
		searchText = (EditText) findViewById(R.id.searchText);
		listView = (ListView) findViewById(R.id.list);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
		setAdapter(friendList.getFriendList());

		searchText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0) {
					adapter.setFriendList(adapter.getOrgFriendList());
				} else {
					List<VUser> friendList = new ArrayList<VUser>();
					String str = s.toString().toLowerCase();
					for (VUser vUser : adapter.getOrgFriendList()) {
						if (vUser.getFullName().toLowerCase().contains(str) || (vUser.getStatus() != null && vUser.getStatus().toLowerCase().contains(str))) {
							friendList.add(vUser);
						}
					}
					adapter.setFriendList(friendList);
				}
				adapter.notifyDataSetChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				VUser vUser = adapter.getItem(position);
				if (!adapter.isInvited(vUser)) {
					CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBox);
					checkbox.setChecked(!checkbox.isChecked());

					adapter.setCheckItem(vUser, checkbox.isChecked());
				}
			}
		});

		selectedListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				VUser vUser = horizontalAdapter.getItem(position);
				adapter.setCheckItem(vUser, false);
				adapter.notifyDataSetChanged();
			}
		});
		horizontalAdapter = new NewGroupChatHorizontalAdapter(this);
		selectedListView.setAdapter(horizontalAdapter);

		btnCreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final List<VUser> selectedUsers = adapter.getSelectedUsers();
				app.addDialogsUsers(selectedUsers);

				if (isInvitation) {
					QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
					if (invitingDialog.getType() == QBDialogType.PRIVATE) {
						// Create new group dialog
						QBDialog dialogToCreate = new QBDialog();
						// dialogToCreate.setName(usersListToChatName(selectedUsers));
						dialogToCreate.setName(VApp.GROUP_CHAT_NAME_NOT_DEFIEND);
						dialogToCreate.setType(QBDialogType.GROUP);
						
						
						ArrayList<Integer> userIds = getUserIds(selectedUsers);
						userIds.addAll(invitingDialog.getOccupants());
						dialogToCreate.setOccupantsIds(userIds);
						QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
							@Override
							public void onSuccess(QBDialog dialog, Bundle args) {
								Intent intent = new Intent();
								setResult(Activity.RESULT_OK, intent);
								intent.putExtra(CREATING_NEW_GROUP, true);
								startGroupChat(dialog);
							}

							@Override
							public void onError(List<String> errors) {
								AlertDialog.Builder dialog = new AlertDialog.Builder(NewGroupChatActivity.this);
								dialog.setMessage("dialog creation errors: " + errors).create().show();
							}
						});
					} else {
						final ArrayList<Integer> userIds = adapter.getSelectedUserIds();
						Integer[] array = userIds.toArray(new Integer[userIds.size()]);
						requestBuilder.push("occupants_ids", array);

						QBChatService.getInstance().getGroupChatManager().updateDialog(invitingDialog, requestBuilder, new QBEntityCallbackImpl<QBDialog>() {
							@Override
							public void onSuccess(QBDialog result, Bundle params) {
								super.onSuccess(result, params);
								Intent intent = new Intent();
								intent.putExtra(INVITING_DIALOG, result);
								setResult(Activity.RESULT_OK, intent);
								intent.putExtra(CREATING_NEW_GROUP, false);
								intent.putIntegerArrayListExtra(INVITED_USER_IDS, userIds);
								finish();
							}

							@Override
							public void onError(List list) {
								AlertDialog.Builder dialog = new AlertDialog.Builder(NewGroupChatActivity.this);
								dialog.setMessage("error when join group chat: " + list.toString()).create().show();
							}
						});
					}
				} else {
					// Create new group dialog
					QBDialog dialogToCreate = new QBDialog();
					// dialogToCreate.setName(usersListToChatName(selectedUsers));
					dialogToCreate.setName(VApp.GROUP_CHAT_NAME_NOT_DEFIEND);
					if (selectedUsers.size() == 1) {
						dialogToCreate.setType(QBDialogType.PRIVATE);
					} else {
						dialogToCreate.setType(QBDialogType.GROUP);
					}
					dialogToCreate.setOccupantsIds(getUserIds(selectedUsers));
					QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
						@Override
						public void onSuccess(QBDialog dialog, Bundle args) {
							if (selectedUsers.size() == 1) {
								startSingleChat(dialog);
							} else {
								startGroupChat(dialog);
							}
						}

						@Override
						public void onError(List<String> errors) {
							AlertDialog.Builder dialog = new AlertDialog.Builder(NewGroupChatActivity.this);
							dialog.setMessage("dialog creation errors: " + errors).create().show();
						}
					});
				}
			}
		});
	}

	public static ArrayList<Integer> getUserIds(List<VUser> users) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (VUser user : users) {
			ids.add(user.getId());
		}
		return ids;
	}

	private void setAdapter(List<VUser> data) {
		if (adapter == null) {
			adapter = new NewGroupChatAdapter(this, data);

			if (isInvitation) {
				Map<Integer, VUser> map = app.getDialogsUsers();
				List<VUser> invitedVUsers = new ArrayList<VUser>();
				for (Integer userId : invitedUsers) {
					invitedVUsers.add(map.get(userId));
				}
				adapter.setInvitedVUsers(invitedVUsers);
			}

			listView.setAdapter(adapter);
		} else {
			adapter.setOrgFriendList(data);
			adapter.notifyDataSetChanged();
		}

	}

	public void addSelectedFriend(VUser vUser) {
		horizontalAdapter.addFriend(vUser);
		horizontalAdapter.notifyDataSetChanged();

		if (horizontalAdapter.getCount() > 0) {
			selectedListView.setVisibility(View.VISIBLE);
			btnCreate.setVisibility(View.VISIBLE);
		} else {
			selectedListView.setVisibility(View.GONE);
			btnCreate.setVisibility(View.GONE);
		}
	}

	public void removeSelectedFriend(VUser vUser) {
		horizontalAdapter.removeFriend(vUser);
		horizontalAdapter.notifyDataSetChanged();
		if (horizontalAdapter.getCount() > 0) {
			selectedListView.setVisibility(View.VISIBLE);
			btnCreate.setVisibility(View.VISIBLE);
		} else {
			selectedListView.setVisibility(View.GONE);
			btnCreate.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (isInvitation) {
				Intent intent = new Intent();
				setResult(Activity.RESULT_CANCELED, intent);
			}
			onBackPressed();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void startSingleChat(QBDialog dialog) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PRIVATE);
		bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);

		ChatActivity.start(this, bundle);
		finish();
	}

	private void startGroupChat(QBDialog dialog) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);
		bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.GROUP);

		ChatActivity.start(this, bundle);
		finish();
	}
}
