package com.example.vconference.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.vconference.R;
import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;

public class ProfileActivity extends Activity {

	static final int EDIT_REQUEST_CODE = 110;
	private TextView id, email, fullname, phone, status;
	private Button accountEditBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);// ??

		// vUser : logged in user
		// Create a layout.xml for this activity, and update
		// Check http://quickblox.com/developers/SimpleSample-users-android in
		// the middle, 'Update own profile'
		final VUser vUser = ((VApp) getApplication()).getUser();

		System.out.println("*****Create ProfileActivity Object*****");

		// Edit 버튼 생성
		accountEditBtn = (Button) findViewById(R.id.account_edit_button);

		// 레이아웃 해당 칸들을 TextView에 연결
		id = (TextView) findViewById(R.id.show_account_ID);
		email = (TextView) findViewById(R.id.show_account_email);
		fullname = (TextView) findViewById(R.id.show_account_fullname);
		phone = (TextView) findViewById(R.id.show_account_phone);
		status = (TextView) findViewById(R.id.show_account_status);

		// 현재 로그인 된 정보를 TextView에 삽입
		id.setText(vUser.getLogin());
		email.setText(vUser.getEmail());
		fullname.setText(vUser.getFullName());
		phone.setText(vUser.getPhone().toString());
		status.setText(vUser.getStatus());

		// EDIT
		accountEditBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 현재 생성된 ProfileActivity에서 ProfileEdit 클래스로 이동
				Intent intent = new Intent(ProfileActivity.this,
						ProfileEditActivity.class);

				startActivityForResult(intent, ProfileEditActivity.EDIT_REQUEST_CODE);
				finish();

			}
		});

	}

}
