package com.example.vconference.ui;

import android.app.Activity;
import android.os.Bundle;

import com.example.vconference.VApp;
import com.example.vconference.custom.objects.VUser;

public class ProfileActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    VUser vUser = ((VApp) getApplication()).getUser();
	    // vUser : logged in user
	    // Create a layout.xml for this activity, and update
	    // Check http://quickblox.com/developers/SimpleSample-users-android in the middle, 'Update own profile'
	}

}
