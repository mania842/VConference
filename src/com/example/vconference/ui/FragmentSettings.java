package com.example.vconference.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.example.vconference.MainActivity;
import com.example.vconference.R;
import com.example.vconference.Settings;
import com.example.vconference.VApp;
import com.example.vconference.ui.adapter.GridSettingsAdapter;
import com.quickblox.auth.QBAuth;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.result.Result;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class FragmentSettings extends Fragment {
	final static public String SETTINGS_LOG_OUT = "settings_log_out";
	private GridView grid_settings;
	private GridSettingsAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View settingsView = inflater.inflate(R.layout.activity_settings, container, false);
		grid_settings = (GridView) settingsView.findViewById(R.id.grid_settings);
		adapter = new GridSettingsAdapter(getActivity());
		grid_settings.setAdapter(adapter);

		grid_settings.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					View v = parent.getChildAt(i);
					v.setBackgroundColor(0xffffffff);
				}
				view.setBackgroundColor(0xffa0d9ff);

				if (adapter.getItem(position) == SettingsItems.LOG_OUT) {
					signOut();
				}
			}

		});
		return settingsView;
	}

	private void signOut() {
		QBUser user = ((VApp) getActivity().getApplication()).getUser();
		final String login = user.getLogin();

		QBUsers.signOut(new QBEntityCallbackImpl() {
			@Override
			public void onSuccess() {
				Settings.getInstance().setSignInAutomatically(false, login, "");
				Settings.getInstance().saveSettings();
				
				Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				
				getActivity().finish();

//				Intent intent = new Intent(getActivity(), MainActivity.class);
//				intent.putExtra(SETTINGS_LOG_OUT, true);
//				startActivity(intent);
//				getActivity().finish();

				

				System.out.println("signOut succeed");
			}

			@Override
			public void onError(List errors) {
				System.out.println(errors);
			}
		});
	}

	public enum SettingsItems {
		ACCOUNT, LOG_OUT
	}
}
