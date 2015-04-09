package com.example.vconference;

import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vconference.custom.objects.UserData;
import com.example.vconference.ui.ChatRoomActivity;
import com.example.vconference.ui.ContainerActivity;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.BaseService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.core.QBVideoChatController;

public class MainActivity extends Activity {
	static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
	private static boolean isFirst = true;

	private QBChatService chatService;
	private Settings settings;
	private EditText editText_user, editText_password;
	private CheckBox checkBox_autoSign;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		try {
			if (BaseService.getBaseService().getToken() != null) {
				Log.i("user", ((VApp) getApplication()).getUser().toString());
				Log.i("BaseService.getBaseService().getToken()", BaseService.getBaseService().getToken());
				
				Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
				startActivity(intent);
				finish();
			}
		} catch (BaseServiceException e) {
			e.printStackTrace();
		}
		filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

		settings = Settings.getInstance();
		editText_user = (EditText) findViewById(R.id.editText_user);
		editText_password = (EditText) findViewById(R.id.editText_password);
		checkBox_autoSign = (CheckBox) findViewById(R.id.checkBox_autoSign);
		checkBox_autoSign.setChecked(settings.isSignInAutomatically());
		if (settings.isSignInAutomatically()) {
			editText_user.setText(settings.getEmail());
			editText_password.setText(settings.getPassword());
		}

		checkBox_autoSign.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String email = editText_user.getText().toString();
				String password = editText_password.getText().toString();
				if (isChecked) {
					settings.setSignInAutomatically(isChecked, email, password);
				} else {
					settings.setSignInAutomatically(isChecked, email, password);
				}
				System.out.println(isChecked);
			}
		});
	}

	private void checkSignInAutomatically() {
		if (!isFirst)
			return;
		if (settings.isSignInAutomatically()) {
			editText_user.setText(settings.getEmail());
			editText_password.setText(settings.getPassword());
			
			signIn(settings.getEmail(), settings.getPassword()); 
		} else {
			editText_user.setText(null);
			editText_password.setText(null);
		}
		isFirst = false;
	}

	private void signIn(String userName, String password) {
		editText_user.setEnabled(false);
		editText_password.setEnabled(false);

		if (!QBChatService.isInitialized()) {
			QBChatService.init(getApplicationContext());
		}

		final QBUser user = new QBUser();
		user.setLogin(userName);
		user.setPassword(password);
		
		QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
			@Override
			public void onSuccess(QBSession session, Bundle params) {
				// success, login to chat
//				Log.e("Session created", "session created, token = " + session.getToken());
				String email = editText_user.getText().toString();
				String password = editText_password.getText().toString();
				settings.setSignInAutomatically(checkBox_autoSign.isChecked(), email, password);
				settings.saveSettings();
				chatService = QBChatService.getInstance();
				user.setId(session.getUserId());
				
				
				

				((VApp) getApplication()).setUser(user);
				loginToChat(user);
			}

			@Override
			public void onError(List<String> errors) {
				editText_user.setEnabled(true);
				editText_password.setEnabled(true);
				System.err.println(errors);
			}
		});

		// QBUsers.signIn(user, new QBEntityCallbackImpl<QBUser>() {
		// @Override
		// public void onSuccess(QBUser result, Bundle params) {
		// super.onSuccess(result, params);
		// settings.saveSettings();
		//
		//
		// chatService = QBChatService.getInstance();
		// VideoConferenceApplication app = (VideoConferenceApplication) getApplicationContext();
		// app.user = result;
		// user.setLogin(user.getLogin());
		// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ " + user);
		// QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>(){
		// @Override
		// public void onSuccess(QBSession session, Bundle args) {
		//
		// // save current user
		// //
		// user.setId(session.getUserId());
		//
		// // login to Chat
		// //
		// loginToChat(user);
		// }
		//
		// @Override
		// public void onError(List<String> errors) {
		// AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		// dialog.setMessage("create session errors: " + errors).create().show();
		// }
		// });
		// }
		//
		// @Override
		// public void onError(List<String> errors) {
		// super.onError(errors);
		// if (errors.contains("login (or email) cannot be blank")) {
		// Toast.makeText(getApplicationContext(), getString(R.string.email_cannot_be_blank), Toast.LENGTH_LONG).show();
		//
		// } else if (errors.contains("password cannot be blank")) {
		// Toast.makeText(getApplicationContext(), getString(R.string.password_cannot_be_blank), Toast.LENGTH_LONG).show();
		//
		// } else if (errors.contains("Unauthorized")) {
		// Toast.makeText(getApplicationContext(), getString(R.string.unauthorized_email_and_password), Toast.LENGTH_LONG).show();
		// }
		// editText_user.setEnabled(true);
		// editText_password.setEnabled(true);
		// System.out.println(errors);
		// }
		// });
	}

	private void loginToChat(final QBUser user) {
		chatService.login(user, new QBEntityCallbackImpl<QBUser>() {
			@Override
			public void onSuccess() {
				try {
					chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
					QBVideoChatController.getInstance().initQBVideoChatMessageListener();
				} catch (SmackException.NotLoggedInException e) {
					e.printStackTrace();
				} catch (XMPPException e) {
					e.printStackTrace();
				}
				// go to Dialogs screen TODO
//				Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
				Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onError(List errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setMessage("chat login errors: " + errors).create().show();
			}
		});
	}

	public void buttonActions(View v) {

		switch (v.getId()) {
		case R.id.btn_signIn:
			signIn(editText_user.getText().toString(), editText_password.getText().toString());
			break;
		case R.id.btn_signUp:
			signUp();
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void signUp() {
		QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
			@Override
			public void onSuccess(QBSession session, Bundle params) {
				final QBUser user = new QBUser("mania842", "lymn8421");
				user.setEmail("asdf@gmail.com");
				user.setFullName("Yong");
				user.setPhone("19175049043");
				user.setCustomDataAsObject(new UserData("�츮��"));

				QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
					@Override
					public void onSuccess(QBUser user, Bundle args) {

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

	private void networkChanged(boolean connected) {
		TextView textView_connection = (TextView) findViewById(R.id.textView_connection);
		textView_connection.setVisibility(connected ? View.GONE : View.VISIBLE);
	}

	// Set When broadcast event will fire.
	private IntentFilter filter = new IntentFilter();
	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
			NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (activeNetInfo != null || mobNetInfo != null) {
				checkSignInAutomatically();
				// Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
				// Toast.makeText(context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
			} else {
				networkChanged(false);
			}
		}
	};

	@Override
	protected void onResume() {
		this.registerReceiver(networkReceiver, filter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Unregister reciever if activity is not in front
		this.unregisterReceiver(networkReceiver);
		super.onPause();
	}

	public void deleteSession() {
		try {
			QBAuth.deleteSession();
		} catch (QBResponseException e) {
			e.printStackTrace();
		}
	}

}
