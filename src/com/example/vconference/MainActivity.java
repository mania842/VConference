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
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vconference.custom.objects.VUser;
import com.example.vconference.ui.ContainerActivity;
import com.example.vconference.ui.FragmentSettings;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.listeners.QBVideoChatSignalingListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.result.Result;
import com.quickblox.core.server.BaseService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.core.QBVideoChatController;
import com.quickblox.videochat.core.objects.QBVideoChat;

public class MainActivity extends Activity {
	static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

	private QBChatService chatService;
	private Settings settings;
	private EditText editText_user, editText_password;
	private CheckBox checkBox_autoSign;
	private ProgressBar progressBar;
	private Button btn_signIn, btn_signUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		btn_signIn = (Button) findViewById(R.id.btn_signIn);
		btn_signUp = (Button) findViewById(R.id.btn_signUp);
		
		btn_signIn.setEnabled(true);
		btn_signUp.setEnabled(true);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
//		deleteSession();
		
		try {
			if (BaseService.getBaseService().getToken() != null) {
//				Log.i("user", ((VApp) getApplication()).getUser().toString());
//				Log.i("BaseService.getBaseService().getToken()", BaseService.getBaseService().getToken());
				
				Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
				startActivity(intent);
				finish();
			}
		} catch (BaseServiceException e) {
//			e.printStackTrace();
		}
		filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

		settings = Settings.getInstance();
		editText_user = (EditText) findViewById(R.id.editText_user);
		editText_password = (EditText) findViewById(R.id.editText_password);
		checkBox_autoSign = (CheckBox) findViewById(R.id.checkBox_autoSign);
		checkBox_autoSign.setChecked(settings.isSignInAutomatically());
		
		checkBox_autoSign.setEnabled(true);
		if (settings.isSignInAutomatically()) {
			editText_user.setText(settings.getLogin());
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
			}
		});
		
	}

	private void checkSignInAutomatically() {
		if (settings.isSignInAutomatically()) {
			editText_user.setText(settings.getLogin());
			editText_password.setText(settings.getPassword());
			
			signIn(settings.getLogin(), settings.getPassword()); 
		} else {
			editText_user.setText(settings.getLogin());
			editText_password.setText(null);
		}
	}

	private void signIn(String userName, String password) {
		progressBar.setVisibility(View.VISIBLE);
		btn_signIn.setEnabled(false);
		btn_signUp.setEnabled(false);
		
		editText_user.setEnabled(false);
		editText_password.setEnabled(false);
		
		checkBox_autoSign.setEnabled(false);

		final VUser user = new VUser();
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
				checkBox_autoSign.setEnabled(true);
				btn_signIn.setEnabled(true);
				btn_signUp.setEnabled(true);
				
				System.err.println(errors);
				progressBar.setVisibility(View.GONE);
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
				Intent intent = new Intent(MainActivity.this, ContainerActivity.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void onError(List errors) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setMessage("chat login errors: " + errors).create().show();
				progressBar.setVisibility(View.GONE);
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
				final VUser user = new VUser("mania842", "lymn8421");
				user.setEmail("asdf@gmail.com");
				user.setFullName("Yong");
				user.setPhone("19175049043");
				user.setStatus("»ì¸®¶ó");

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

	@SuppressWarnings("deprecation")
	public void deleteSession() {
		QBChatService.getInstance().logout(new QBEntityCallback<QBUser>() {
			@Override
			public void onError(List<String> arg0) {
			}

			@Override
			public void onSuccess() {
			}

			@Override
			public void onSuccess(QBUser arg0, Bundle arg1) {
			}
		});
		
		QBAuth.deleteSession(new QBCallback() {
			@Override
			public void onComplete(Result arg0, Object arg1) {
				System.out.println("onComplete");
			}
			
			@Override
			public void onComplete(Result arg0) {
				System.out.println("on Complete 2");
			}
		});
	}
}
