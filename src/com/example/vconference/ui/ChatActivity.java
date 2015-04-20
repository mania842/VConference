package com.example.vconference.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.adapter.ChatAdapter;
import com.example.vconference.ui.adapter.ContactListAdapter;
import com.example.vconference.ui.core.ChatManager;
import com.example.vconference.ui.core.GroupChatManagerImpl;
import com.example.vconference.ui.core.PrivateChatManagerImpl;
import com.example.vconference.ui.view.OpponentSurfaceView;
import com.example.vconference.ui.view.OwnSurfaceView;
import com.navdrawer.SimpleSideDrawer;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.videochat.core.QBVideoChatController;
import com.quickblox.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.videochat.model.objects.CallState;
import com.quickblox.videochat.model.objects.CallType;
import com.quickblox.videochat.model.objects.VideoChatConfig;

public class ChatActivity extends Activity {
	private static final String TAG = ChatActivity.class.getSimpleName();
	public static final String VIDEO = "VIDEO";
	public static final String VIDEO_STARTED = "VIDEO STARTED";
	public static final String VIDEO_ENDED = "VIDEO ENDED";

	public static final String INVITING_USER = "INVITING_USER";
	public static final String INVITED_USERS = "INVITED_USERS";

	public static final String EXTRA_MODE = "mode";
	public static final String EXTRA_DIALOG = "dialog";
	private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

	public static final int REQUEST_CODE = 102;

	private EditText messageEditText;
	private ListView messagesContainer;
	private Button sendButton;
	private ProgressBar progressBar;

	private Mode mode = Mode.PRIVATE;
	private ChatManager chat;
	private ChatAdapter adapter;
	private QBDialog dialog;

	private ArrayList<QBChatMessage> history;

	private OpponentSurfaceView opponentView;
	private OwnSurfaceView myView;
	private VideoChatConfig videoChatConfig;

	private SimpleSideDrawer slide_me;
	private ListView contactList;
	private ContactListAdapter adapterContact;
	private VApp app;
	private VUser myUser;

	private boolean isAdmin;
	private boolean isCameraSharing;
	private Integer cameraSharingUserId;
	private LinearLayout startVideo;
	private LinearLayout cameraSwitch;
	private View line2;

	public static void start(Context context, Bundle bundle) {
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		app = (VApp) getApplication();
		myUser = app.getUser();
		initViews();
		initVideoCalls();

		if (dialog.getType() == QBDialogType.PRIVATE)
			getActionBar().setTitle(app.getUserNameById(dialog.getUserId()));
		else if (dialog.getType() == QBDialogType.GROUP) {
			if (dialog.getName().equals(VApp.GROUP_CHAT_NAME_NOT_DEFIEND)) {
				getActionBar().setTitle(getString(R.string.group_chat));
			} else {
				getActionBar().setTitle(dialog.getName());
			}

		}

		isAdmin = dialog.getUserId().equals(app.getUser().getId());
		if (isAdmin) {
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		initRightMenuView();

		refreshCameraInfo();
	}

	private void initRightMenuView() {
		slide_me = new SimpleSideDrawer(this);
		slide_me.setRightBehindContentView(R.layout.right_menu);
		contactList = (ListView) slide_me.findViewById(R.id.contactList);
		ArrayList<VUser> vUsers = ((VApp) getApplication()).getVUsersWithoutMe(dialog.getOccupants());
		adapterContact = new ContactListAdapter(vUsers, this, dialog);

		View line = slide_me.findViewById(R.id.line);
		line2 = slide_me.findViewById(R.id.line2);
		line2.setVisibility(View.GONE);
		startVideo = (LinearLayout) slide_me.findViewById(R.id.startVideo);
		if (isAdmin) {
			startVideo.setVisibility(View.VISIBLE);
			line.setVisibility(View.VISIBLE);
		} else {
			startVideo.setVisibility(View.GONE);
			line.setVisibility(View.GONE);
		}
		startVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView txtTitle = (TextView) startVideo.findViewById(R.id.txt_title);
				if (txtTitle.getText().equals("Start Video")) {
					setVideoObjectForDialog(true);
					txtTitle.setText("Stop Video");
				} else {
					setVideoObjectForDialog(false);
					txtTitle.setText("Start Video");
				}
			}
		});
		contactList.setAdapter(adapterContact);

		final Button btn_invite = (Button) slide_me.findViewById(R.id.btn_invite);
		btn_invite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChatActivity.this, NewGroupChatActivity.class);
				intent.putExtra(NewGroupChatActivity.MODE_INVITE, true);
				intent.putIntegerArrayListExtra(NewGroupChatActivity.INVITE_OCCUPANTS, dialog.getOccupants());
				intent.putExtra(NewGroupChatActivity.INVITING_DIALOG, dialog);

				startActivityForResult(intent, NewGroupChatActivity.REQUEST_CODE);
			}
		});

		final LinearLayout leaveChat = (LinearLayout) slide_me.findViewById(R.id.leaveChat);
		leaveChat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chat.leaveChat(dialog, new QBEntityCallbackImpl<Void>() {
					@Override
					public void onSuccess() {
						onBackPressed();
					}

					@Override
					public void onError(List<String> errors) {
						System.out.println(errors);
					}
				});
			}
		});

		cameraSwitch = (LinearLayout) slide_me.findViewById(R.id.cameraSwitch);
		cameraSwitch.setVisibility(View.GONE);
		cameraSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myView.switchCamera();
			}
		});
	}

	public void refreshCameraInfo() {
		QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
		requestBuilder.setPagesLimit(5);
		requestBuilder.eq("dialogId", dialog.getDialogId());
		QBCustomObjects.getObjects("DialogInfo", requestBuilder, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {

			@Override
			public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
				if (customObjects.size() > 0) {
					QBCustomObject record = customObjects.get(0);
					HashMap<String, Object> fields = record.getFields();
					boolean isChanged = isCameraSharing;
					cameraSharingUserId = Integer.parseInt(fields.get("cameraSharingId").toString());
					isCameraSharing = ((String) fields.get("cameraSharing")).equalsIgnoreCase("True");

					isChanged = isChanged != isCameraSharing;
					// System.out.println("camera sharing " + isCameraSharing);
					// System.out.println("isChanged " + isChanged);
					// System.out.println("cameraSharingId " + cameraSharingId);
					// System.out.println(dialog.getUserId() + " " + app.getUserNameById(dialog.getUserId()));

					if (isChanged) {
						if (isCameraSharing) {

							if (!cameraSharingUserId.equals(myUser.getId())) {
								VUser opponentUser = new VUser(cameraSharingUserId);
								videoChatConfig = QBVideoChatController.getInstance().callFriend(opponentUser, CallType.VIDEO_AUDIO, null);
							}
							// myView.setVisibility(View.VISIBLE);

							// System.out.println("camera sharing " + isCameraSharing);
							// System.out.println(dialog.getUserId() + " " + app.getUserNameById(dialog.getUserId()));
						} else {
							if (!cameraSharingUserId.equals(myUser.getId())) {
								if (videoChatConfig != null)
									QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);
								opponentView.clear();
								opponentView.setVisibility(View.GONE);								
							}
						}
					}
				}
			}

			@Override
			public void onError(List<String> errors) {
				System.out.println(errors);

			}
		});
	}

	private void setVideoObjectForDialog(final boolean isStart) {
		if (isStart) {
			myView.setVisibility(View.VISIBLE);
			cameraSwitch.setVisibility(View.VISIBLE);
			line2.setVisibility(View.VISIBLE);
			
			myView.openCamera();
		} else {
			myView.closeCamera();
			myView.setVisibility(View.GONE);
			cameraSwitch.setVisibility(View.GONE);
			line2.setVisibility(View.GONE);
		}

		QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
		requestBuilder.setPagesLimit(5);
		requestBuilder.eq("dialogId", dialog.getDialogId());

		QBCustomObjects.getObjects("DialogInfo", requestBuilder, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {

			@Override
			public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
				if (customObjects.size() == 0) {
					QBCustomObject newRecord = new QBCustomObject();

					// put fields
					newRecord.putString("dialogId", dialog.getDialogId());
					newRecord.putBoolean("cameraSharing", isStart);
					newRecord.putInteger("cameraSharingId", myUser.getId());

					// set the class name
					newRecord.setClassName("DialogInfo");

					QBCustomObjects.createObject(newRecord, new QBEntityCallbackImpl<QBCustomObject>() {
						@Override
						public void onSuccess(QBCustomObject createdObject, Bundle params) {
							sendMessageForVideo(isStart);
						}

						@Override
						public void onError(List<String> errors) {
						}
					});
				} else {
					QBCustomObject record = customObjects.get(0);
					record.setClassName("DialogInfo");
					HashMap<String, Object> fields = new HashMap<String, Object>();
					fields.put("cameraSharing", isStart);
					fields.put("cameraSharingId", myUser.getId());
					record.setFields(fields);

					QBCustomObjects.updateObject(record, new QBEntityCallbackImpl<QBCustomObject>() {

						@Override
						public void onSuccess(QBCustomObject object, Bundle params) {
							sendMessageForVideo(isStart);
						}

						@Override
						public void onError(List<String> errors) {
							System.out.println(errors);
						}
					});
				}
			}

			@Override
			public void onError(List<String> errors) {

			}
		});
	}

	private void sendMessageForVideo(boolean isStart) {
		// Send chat message
		//
		QBChatMessage chatMessage = new QBChatMessage();
		if (isStart) {
			chatMessage.setProperty(VIDEO, VIDEO_STARTED);
		} else {
			chatMessage.setProperty(VIDEO, VIDEO_ENDED);
		}
		chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
		chatMessage.setDateSent(new Date().getTime() / 1000);

		try {
			chat.sendMessage(chatMessage);
		} catch (XMPPException e) {
			Log.e(TAG, "failed to send a message", e);
		} catch (SmackException sme) {
			Log.e(TAG, "failed to send a message", sme);
		}

		messageEditText.setText("");
		if (mode == Mode.PRIVATE) {
			showMessage(chatMessage);
		}
	}

	public void initVideoCalls() {
		opponentView = (OpponentSurfaceView) findViewById(R.id.opponentView);
		myView = (OwnSurfaceView) findViewById(R.id.cameraView);

		opponentView.setVisibility(View.GONE);
		myView.setVisibility(View.GONE);

		try {
			Display display = getWindowManager().getDefaultDisplay();
			int deviceHeight = display.getHeight(); // deprecated

			Camera camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = parameters.getPictureSize();

			int cameraHeight = size.height;
			int cameraWidth = size.width;

			float rate = (float) cameraHeight / (float) cameraWidth;
			int viewHeight = (int) (deviceHeight * 0.3);
			int viewWidth = (int) (viewHeight * rate);
			myView.getLayoutParams().width = viewWidth;
			myView.getLayoutParams().height = viewHeight;
			opponentView.getLayoutParams().width = viewWidth;
			opponentView.getLayoutParams().height = viewHeight;
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		// VideoChat settings
		videoChatConfig = getIntent().getParcelableExtra(VideoChatConfig.class.getCanonicalName());

		myView.setCameraDataListener(new OwnSurfaceView.CameraDataListener() {
			@Override
			public void onCameraDataReceive(byte[] data) {
				if (videoChatConfig != null && videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
					return;
				}
				QBVideoChatController.getInstance().sendVideo(data);
			}
		});

		// Set video chat listener
		VUser videoUser = ((VApp) getApplication()).getUser();

		try {
			QBVideoChatController.getInstance().setQBVideoChatListener(videoUser, qbVideoChatListener);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	private void clearChatActivity() {
		if (myView != null) {
			myView.closeCamera();
			if (myView.getVisibility() == View.VISIBLE)
				setVideoObjectForDialog(false);
		}
		if (videoChatConfig != null)
			QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);

		Intent intent = new Intent();
		intent.putExtra(EXTRA_DIALOG, dialog);
		setResult(Activity.RESULT_OK, intent);
	}

	@Override
	public void onBackPressed() {
		try {
			clearChatActivity();
			chat.release();
		} catch (XMPPException e) {
			Log.e(TAG, "failed to release chat", e);
		}
		super.onBackPressed();
	}

	// @Override
	// public void finish() {
	// try {
	// clearChatActivity();
	// chat.release();
	// } catch (XMPPException e) {
	// Log.e(TAG, "failed to release chat", e);
	// }
	// super.finish();
	// }

	private void initViews() {
		messagesContainer = (ListView) findViewById(R.id.messagesContainer);
		messageEditText = (EditText) findViewById(R.id.messageEdit);
		sendButton = (Button) findViewById(R.id.chatSendButton);

		RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		Intent intent = getIntent();

		// Get chat dialog
		//
		dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);

		mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
		switch (mode) {
		case PUBLIC_GROUP:
		case GROUP:
			chat = new GroupChatManagerImpl(this);

			// Join group chat
			//
			progressBar.setVisibility(View.VISIBLE);
			//
			((GroupChatManagerImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl() {
				@Override
				public void onSuccess() {

					// Load Chat history
					//
					loadChatHistory();
				}

				@Override
				public void onError(List list) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
					dialog.setMessage("error when join group chat: " + list.toString()).create().show();
				}
			});

			break;
		case PRIVATE:
			Integer opponentID = ((VApp) getApplication()).getOpponentIDForPrivateDialog(dialog);

			chat = new PrivateChatManagerImpl(this, opponentID);

			// Load CHat history
			//
			loadChatHistory();
			break;
		}

		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String messageText = messageEditText.getText().toString();
				if (TextUtils.isEmpty(messageText)) {
					return;
				}

				// Send chat message
				//
				QBChatMessage chatMessage = new QBChatMessage();
				chatMessage.setBody(messageText);
				chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
				chatMessage.setDateSent(new Date().getTime() / 1000);

				try {
					chat.sendMessage(chatMessage);
				} catch (XMPPException e) {
					Log.e(TAG, "failed to send a message", e);
				} catch (SmackException sme) {
					Log.e(TAG, "failed to send a message", sme);
				}

				messageEditText.setText("");

				if (mode == Mode.PRIVATE) {
					showMessage(chatMessage);
				}
			}
		});
	}

	private void loadChatHistory() {
		QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
		customObjectRequestBuilder.setPagesLimit(100);
		customObjectRequestBuilder.sortDesc("date_sent");

		QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
			@Override
			public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
				history = messages;

				adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBChatMessage>());
				messagesContainer.setAdapter(adapter);

				for (int i = messages.size() - 1; i >= 0; --i) {
					QBChatMessage msg = messages.get(i);
					showMessage(msg);
				}

				progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onError(List<String> errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
				dialog.setMessage("load chat history errors: " + errors).create().show();
			}
		});
	}

	public void showMessage(final QBChatMessage message) {
		adapter.add(message);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
				scrollDown();

				Map<String, String> chatMap = message.getProperties();
				if (chatMap.containsKey(ChatActivity.VIDEO)) {
					refreshCameraInfo();
				}
			}
		});
	}

	private void scrollDown() {
		messagesContainer.setSelection(messagesContainer.getCount() - 1);
	}

	OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {

		@Override
		public void onCameraDataReceive(byte[] videoData) {
			//
		}

		@Override
		public void onMicrophoneDataReceive(byte[] audioData) {
			QBVideoChatController.getInstance().sendAudio(audioData);
		}

		@Override
		public void onOpponentVideoDataReceive(final byte[] videoData) {
			opponentView.render(videoData);
		}

		@Override
		public void onOpponentAudioDataReceive(byte[] audioData) {
			QBVideoChatController.getInstance().playAudio(audioData);
		}

		@Override
		public void onProgress(boolean progress) {
			// progressBar.setVisibility(progress ? View.VISIBLE : View.GONE);
		}

		@Override
		public void onVideoChatStateChange(CallState callState, VideoChatConfig receivedVideoChatConfig) {
			videoChatConfig = receivedVideoChatConfig;

			switch (callState) {
			case ON_CALL_START:
				Toast.makeText(getBaseContext(), "ON_CALL_START", Toast.LENGTH_SHORT).show();

				progressBar.setVisibility(View.INVISIBLE);
				break;
			case ON_CANCELED_CALL:
				Toast.makeText(getBaseContext(), "ON_CANCELED_CALL", Toast.LENGTH_SHORT).show();

				videoChatConfig = null;
				// if (alertDialog != null && alertDialog.isShowing()) {
				// alertDialog.dismiss();
				// }
				// autoCancelHandler.removeCallbacks(autoCancelTask);

				break;
			case ON_CALL_END:
				Toast.makeText(getBaseContext(), "ON_CALL_END", Toast.LENGTH_SHORT).show();
				opponentView.setVisibility(View.GONE);
				// clear opponent view
				opponentView.clear();

				// startStopVideoCallBtn.setText("Call user");
				break;
			case ACCEPT:
				Toast.makeText(getBaseContext(), "ACCEPT", Toast.LENGTH_SHORT).show();
				QBVideoChatController.getInstance().acceptCallByFriend(videoChatConfig, null);
				// showIncomingCallDialog();
				break;
			case ON_ACCEPT_BY_USER:
				Toast.makeText(getBaseContext(), "ON_ACCEPT_BY_USER", Toast.LENGTH_SHORT).show();
				QBVideoChatController.getInstance().onAcceptFriendCall(videoChatConfig, null);
				if (!cameraSharingUserId.equals(myUser.getId()))
					opponentView.setVisibility(View.VISIBLE);
				break;
			case ON_REJECTED_BY_USER:
				Toast.makeText(getBaseContext(), "ON_REJECTED_BY_USER", Toast.LENGTH_SHORT).show();

				progressBar.setVisibility(View.INVISIBLE);
				break;
			case ON_CONNECTED:
				Toast.makeText(getBaseContext(), "ON_CONNECTED", Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.INVISIBLE);

				// startStopVideoCallBtn.setText("Hung up");
				break;
			case ON_START_CONNECTING:
				Toast.makeText(getBaseContext(), "ON_START_CONNECTING", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public static enum Mode {
		PRIVATE, GROUP, PUBLIC_GROUP
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		System.err.println("orientation changed");
		// if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		// } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
		// Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		// }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rooms, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_more) {
			slide_me.toggleRightDrawer();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NewGroupChatActivity.REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				boolean creatingNewGroup = data.getBooleanExtra(NewGroupChatActivity.CREATING_NEW_GROUP, false);
				if (creatingNewGroup) {
					finish();
				} else {
					dialog = (QBDialog) data.getSerializableExtra(NewGroupChatActivity.INVITING_DIALOG);
					ArrayList<Integer> invitedUserIds = data.getIntegerArrayListExtra(NewGroupChatActivity.INVITED_USER_IDS);
					initRightMenuView();
					slide_me.toggleRightDrawer();

					QBChatMessage chatMessage = new QBChatMessage();
					VUser me = app.getUser();
					chatMessage.setProperty(INVITING_USER, String.valueOf(me.getId()));

					String userIdsStr = "";
					for (Integer userId : invitedUserIds) {
						userIdsStr += userId + " ";
					}
					chatMessage.setProperty(INVITED_USERS, userIdsStr.trim());
					chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
					chatMessage.setDateSent(new Date().getTime() / 1000);

					try {
						chat.sendMessage(chatMessage);
					} catch (XMPPException e) {
						Log.e(TAG, "failed to send a message", e);
					} catch (SmackException sme) {
						Log.e(TAG, "failed to send a message", sme);
					}

					if (mode == Mode.PRIVATE) {
						showMessage(chatMessage);
					}
				}
			}
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

}
