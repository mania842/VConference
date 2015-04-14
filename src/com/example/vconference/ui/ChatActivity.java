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
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.videochat.core.QBVideoChatController;
import com.quickblox.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.videochat.model.objects.CallState;
import com.quickblox.videochat.model.objects.VideoChatConfig;

public class ChatActivity extends Activity {
	private static final String TAG = ChatActivity.class.getSimpleName();
	public static final String VIDEO = "VIDEO";
	public static final String VIDEO_STARTED = "VIDEO STARTED";
	public static final String VIDEO_ENDED = "VIDEO ENDED";

	public static final String EXTRA_MODE = "mode";
	public static final String EXTRA_DIALOG = "dialog";
	private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

	private EditText messageEditText;
	private ListView messagesContainer;
	private Button sendButton;
	private ProgressBar progressBar;

	private Mode mode = Mode.PRIVATE;
	private ChatManager chat;
	private ChatAdapter adapter;
	private QBDialog dialog;

	private ArrayList<QBChatMessage> history;

	private Button addButton;
	private Button getOccupants;

	private OpponentSurfaceView opponentView;
	private OwnSurfaceView myView;
	private VideoChatConfig videoChatConfig;

	private SimpleSideDrawer slide_me;
	private ListView contactList;
	private ContactListAdapter adapterContact;
	private VApp app;

	private boolean isAdmin;
	private boolean isCameraSharing;
	private LinearLayout startVideo;

	public static void start(Context context, Bundle bundle) {
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		app = (VApp) getApplication();
		initViews();
		initVideoCalls();

		if (dialog.getUserId().equals(app.getUser().getId())) {
			isAdmin = true;
		} else {
			isAdmin = false;
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

		startVideo = (LinearLayout) slide_me.findViewById(R.id.startVideo);
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
	}
	
	public void refreshCameraInfo() {
		QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
		requestBuilder.setPagesLimit(5);
		requestBuilder.eq("dialogJid", dialog.getRoomJid());
		QBCustomObjects.getObjects("DialogInfo", requestBuilder, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
			@Override
			public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
				if (customObjects.size() > 0) {
					QBCustomObject record = customObjects.get(0);
					HashMap<String, Object> fields = record.getFields();
					boolean isChanged = isCameraSharing;
					isCameraSharing = ((String) fields.get("cameraSharing")).equalsIgnoreCase("True");
					isChanged = isChanged != isCameraSharing;
					
					if (isChanged) {
						//TODO
					}
					
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@ isCameraSharing @@@@@@@@@@@@@@@@@ " + fields.get("cameraSharing") + " " + isCameraSharing);
				}
			}
			@Override
			public void onError(List<String> errors) {

			}
		});
	}

	private void setVideoObjectForDialog(final boolean isStart) {
		if (isStart) {
			myView.setVisibility(View.VISIBLE);
			myView.openCamera();
		} else {
			myView.closeCamera();
			myView.setVisibility(View.GONE);
		}

		QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
		requestBuilder.setPagesLimit(5);
		requestBuilder.eq("dialogJid", dialog.getRoomJid());

		QBCustomObjects.getObjects("DialogInfo", requestBuilder, new QBEntityCallbackImpl<ArrayList<QBCustomObject>>() {
			@Override
			public void onSuccess(ArrayList<QBCustomObject> customObjects, Bundle params) {
				if (customObjects.size() == 0) {
					QBCustomObject newRecord = new QBCustomObject();

					// put fields
					newRecord.putString("dialogJid", dialog.getRoomJid());
					newRecord.putBoolean("cameraSharing", isStart);

					// set the class name
					newRecord.setClassName("DialogInfo");

					QBCustomObjects.createObject(newRecord, new QBEntityCallbackImpl<QBCustomObject>() {
						@Override
						public void onSuccess(QBCustomObject createdObject, Bundle params) {
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
					record.setFields(fields);
					
					QBCustomObjects.updateObject(record, new QBEntityCallbackImpl<QBCustomObject>(){
						@Override
						public void onSuccess(QBCustomObject object, Bundle params) {
							
						}
						@Override
					    public void onError(List<String> errors) {
					 
					    }
					});
				}
			}

			@Override
			public void onError(List<String> errors) {

			}
		});
		// QBChatService.getInstance().getGroupChatManager().getGroupChat(dialog.getRoomJid())
		// Send chat message
		//
		QBChatMessage chatMessage = new QBChatMessage();
		if (isStart)
			chatMessage.setProperty(VIDEO, VIDEO_STARTED);
		else
			chatMessage.setProperty(VIDEO, VIDEO_ENDED);
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
		// VideoChat settings
		// videoChatConfig = getIntent().getParcelableExtra(VideoChatConfig.class.getCanonicalName());
		//
		// QBUser opponentUser = new QBUser(2758477);
		//
		// videoChatConfig = QBVideoChatController.getInstance().callFriend(opponentUser, CallType.VIDEO_AUDIO, null);
		//
		// myView.setCameraDataListener(new OwnSurfaceView.CameraDataListener() {
		// @Override
		// public void onCameraDataReceive(byte[] data) {
		// if (videoChatConfig != null && videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
		// return;
		// }
		// QBVideoChatController.getInstance().sendVideo(data);
		// }
		// });
		//
		// // Set video chat listener
		//
		// QBUser videoUser = ((VApp) getApplication()).getUser();
		//
		// try {
		// QBVideoChatController.getInstance().setQBVideoChatListener(videoUser, qbVideoChatListener);
		// } catch (XMPPException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void onBackPressed() {
		try {
			if (myView != null) {
				myView.closeCamera();
				if (myView.getVisibility() == View.VISIBLE)
					setVideoObjectForDialog(false);
			}
			if (videoChatConfig != null)
				QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);
			chat.release();
		} catch (XMPPException e) {
			Log.e(TAG, "failed to release chat", e);
		}
		super.onBackPressed();
	}

	private void initViews() {
		messagesContainer = (ListView) findViewById(R.id.messagesContainer);
		messageEditText = (EditText) findViewById(R.id.messageEdit);
		sendButton = (Button) findViewById(R.id.chatSendButton);

		addButton = (Button) findViewById(R.id.chatAddButton);
		getOccupants = (Button) findViewById(R.id.getoccupants);

		TextView meLabel = (TextView) findViewById(R.id.meLabel);
		TextView companionLabel = (TextView) findViewById(R.id.companionLabel);
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
			container.removeView(meLabel);
			container.removeView(companionLabel);

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

			companionLabel.setText(((VApp) getApplication()).getDialogsUsers().get(opponentID).getLogin());

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

		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((GroupChatManagerImpl) chat).inviteUser(dialog, 2759018, new QBEntityCallbackImpl() {
					@Override
					public void onSuccess() {
						for (Integer userID : dialog.getOccupants()) {

							QBChatMessage chatMessage = GroupChatManagerImpl.createChatNotificationForGroupChatUpdate(dialog);
							long time = new Date().getTime();
							chatMessage.setProperty("date_sent", time + "");
							QBChatService chatService = QBChatService.getInstance();
							QBPrivateChat chat = QBChatService.getInstance().getPrivateChatManager().getChat(userID);
							if (chat == null) {
								chat = chatService.getPrivateChatManager().createChat(userID, null);
							}

							try {
								chat.sendMessage(chatMessage);
							} catch (Exception e) {
								// error
							}
						}
					}

					@Override
					public void onError(List list) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
						dialog.setMessage("error when join group chat: " + list.toString()).create().show();
					}
				});

			}
		});
		getOccupants.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("Clicked");
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				// System.out.println(((GroupChatManagerImpl) chat).getRoomUsers());
				System.out.println(((GroupChatManagerImpl) chat).getOnlineUsers());
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

	public void showMessage(QBChatMessage message) {
		adapter.add(message);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
				scrollDown();
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
}
