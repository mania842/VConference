package com.example.vconference;

import java.util.List;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class VideoConferenceApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		QBSettings.getInstance().fastConfigInit("19109", "NC8QdBBMVxdbmuv", "dMKqMTsV7JvAdMK");
		QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
		    @Override
		     public void onSuccess(QBSession session, Bundle params) {
		        Log.i("ASDF", "session created, token = " + session.getToken());
		     }
		     @Override
		     public void onError(List<String> errors) {
		 
		     }
		});
	}

}
