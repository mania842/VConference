package com.example.vconference.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class ProfileEditActivity extends Activity {

	static final int EDIT_REQUEST_CODE = 110;

	static final String ID = "ID";
	static final String E_MAIL = "E_MAIL";
	static final String FULL_NAME = "FULL_NAME";
	static final String PHONE = "PHONE";
	static final String STATUS = "STATUS";
	static final String VUSER = "VUSER";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);

		// 현재 로그인 된 유저 정보
		final VUser vUser = ((VApp) getApplication()).getUser();

		TextView etID = (TextView) findViewById(R.id.show_edit_account_ID);
		final EditText etEmail = (EditText) findViewById(R.id.edittext_account_email);
		final EditText etFullname = (EditText) findViewById(R.id.edittext_account_fullname);
		final EditText etPhone = (EditText) findViewById(R.id.edittext_account_phone);
		final EditText etStatus = (EditText) findViewById(R.id.edittext_account_status);

		// 스크린에 보여주는 부분
		etID.setText(vUser.getLogin());
		etEmail.setText(vUser.getEmail());
		etFullname.setText(vUser.getFullName());
		etPhone.setText(vUser.getPhone());
		etStatus.setText(vUser.getStatus());

		Button updateBtn = (Button) findViewById(R.id.account_edit_update_button);

		updateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				vUser.setEmail(etEmail.getText().toString());
				vUser.setFullName(etFullname.getText().toString());
				vUser.setPhone(etPhone.getText().toString());
				vUser.setStatus(etStatus.getText().toString());

				QBUsers.updateUser(vUser, new QBEntityCallbackImpl<QBUser>() {
					@Override
					public void onSuccess(QBUser user, Bundle args) {
						System.out.println("*****UPDATE SUCCESS*****");

						Toast.makeText(ProfileEditActivity.this, "Update Success",
								Toast.LENGTH_LONG).show();

						Intent intent = new Intent(ProfileEditActivity.this,
								ProfileActivity.class);

						startActivityForResult(intent,
								ProfileActivity.EDIT_REQUEST_CODE);
						finish();
					}

					@Override
					public void onError(List<String> errors) {
						System.out.println("*****UPDATE ERROR*****");

						Toast.makeText(ProfileEditActivity.this,
								"There is no ID in Database", Toast.LENGTH_LONG)
								.show();
					}
				});
			}
		});

		// TODO Auto-generated method stub
	}

}
