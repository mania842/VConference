package com.example.vconference;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

	}

	public void buttonActions(View v) {
		
		switch (v.getId()) {
		case R.id.btn_signIn:
			EditText editText_email = (EditText) findViewById(R.id.editText_email);
			EditText editText_password = (EditText) findViewById(R.id.editText_password);
			CheckBox checkBox_autoSign = (CheckBox) findViewById(R.id.checkBox_autoSign);
			
//			QBUser user = new QBUser();
			
			
			final QBUser user = new QBUser();
			user.setEmail(editText_email.getText().toString());
			user.setPassword(editText_password.getText().toString());
//			user.setEmail("dev.yong842@gmail.com");
//			user.setPassword("lymn8421");

			
			
			
			QBUsers.signIn(user, new QBEntityCallbackImpl<QBUser>() {
				@Override
				public void onError(List<String> errors) {
					super.onError(errors);
					System.out.println("Failed @@@@@@@@@@@@@@@@@@@");
					System.out.println(errors);
				}

				@Override
				public void onSuccess(QBUser result, Bundle params) {
					super.onSuccess(result, params);
					
					System.out.println("success @@@@@@@@@@@@@@@@@@@");
					System.out.println(result);
				}
			});
			
			
//			System.out.println(editText_email.getText());
//			System.out.println(editText_password.getText());
//			System.out.println(checkBox_autoSign.isSelected());
//			System.out.println(checkBox_autoSign.isChecked());
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
		final QBUser user = new QBUser("Javck@aaa", "javckpassword");
		user.setExternalId("45345");
		user.setFacebookId("100233453457767");
		user.setTwitterId("182334635457");
		user.setEmail("Javck@mail.com");
		user.setFullName("Javck Bold");
		user.setPhone("+18904567812");
		StringifyArrayList<String> tags = new StringifyArrayList<String>();
		tags.add("car");
		tags.add("man");
		user.setTags(tags);
		user.setWebsite("www.mysite.com");
		 
		QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
		    @Override
		    public void onSuccess(QBUser user, Bundle args) {
		 
		    }
		 
		    @Override
		    public void onError(List<String> errors) {
		    }
		});

	}

}
