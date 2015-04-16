package com.example.vconference;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vconference.custom.objects.VUser;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignupActivity extends Activity {

	static final int NUMBER_OF_MIN_PASSWORD = 8;
	static final int NUMBER_OF_MIN_FULLNAME = 3;
	static final int NUMBER_OF_MIN_ID = 2;

	static final String FULL_NAME = "FULL_NAME";
	static final String ID = "ID";
	static final String PASSWORD = "PASSWORD";
	static final String PASSWORD_CONFIRM = "PASSWORD_CONFIRM";
	static final String PHONE = "PHONE";
	static final String EMAIL = "EMAIL";
	static final String NICK_NAME = "NICK_NAME";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		Button createIDBtn = (Button) findViewById(R.id.btn_createID);
		Button cancelBtn = (Button) findViewById(R.id.btn_cancel);

		createIDBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText newFullName = (EditText) findViewById(R.id.signup_fullname);
				final EditText newID = (EditText) findViewById(R.id.signup_ID);
				final EditText newPassword = (EditText) findViewById(R.id.signup_password);
				final EditText newPasswordConfirm = (EditText) findViewById(R.id.signup_confirm_password);
				final EditText newPhone = (EditText) findViewById(R.id.signup_phone);
				final EditText newEmail = (EditText) findViewById(R.id.signup_email);
				final EditText newNickName = (EditText) findViewById(R.id.signup_nickname);

				Pattern pattern = Pattern.compile("[+0-9]");
				Matcher matcher = pattern.matcher((String) newPhone.getText().toString());

				if (newFullName.getText().toString().equals("") || newID.getText().toString().equals("") || newPassword.getText().toString().equals("")
						|| newPasswordConfirm.getText().toString().equals("") || newPhone.getText().toString().equals("")
						|| newEmail.getText().toString().equals("") || newNickName.getText().toString().equals("")) {
					Toast.makeText(SignupActivity.this, "Please fill every elements", Toast.LENGTH_LONG).show();
				} else if (newFullName.getText().toString().length() < NUMBER_OF_MIN_FULLNAME) {
					Toast.makeText(SignupActivity.this, "Full name is too short (minimum is 3 characters)", Toast.LENGTH_LONG).show();
				} else if (newID.getText().toString().length() < NUMBER_OF_MIN_ID) {
					Toast.makeText(SignupActivity.this, "ID is too short (minimum is 2 characters)", Toast.LENGTH_LONG).show();
				} else if (matcher.find() == false) {
					Toast.makeText(SignupActivity.this, "Phone should have only number!", Toast.LENGTH_LONG).show();
				} else if (newPassword.getText().toString().equals(newPasswordConfirm.getText().toString()) == false) {
					Toast.makeText(SignupActivity.this, "Not matched between Password and PW Confirm", Toast.LENGTH_LONG).show();

				} else if (newPassword.getText().toString().length() < NUMBER_OF_MIN_PASSWORD
						|| newPassword.getText().toString().length() < NUMBER_OF_MIN_PASSWORD) {
					Toast.makeText(SignupActivity.this, "Password is too short (minimum is 8 characters)", Toast.LENGTH_LONG).show();
				} else {
					QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
						@Override
						public void onSuccess(QBSession session, Bundle params) {
							final VUser user = new VUser(newID.getText().toString(), newPassword.getText().toString());
							user.setEmail(newEmail.getText().toString());
							user.setFullName(newFullName.getText().toString());
							user.setPhone(newPhone.getText().toString());
							user.setStatus(newNickName.getText().toString());

							QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
								@Override
								public void onSuccess(QBUser user, Bundle args) {
									finish();
								}

								@Override
								public void onError(List<String> errors) {

									if (errors.size() > 0) {
										String error = errors.get(0);
										Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
									}
									System.out.println(errors);
								}
							});
						}

						@Override
						public void onError(List<String> errors) {
						}
					});

				}

			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}
}
