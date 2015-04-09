package com.example.vconference.ui.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vconference.R;

public class Windows extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View android = inflater.inflate(R.layout.main_frag_window, container, false);
		((TextView) android.findViewById(R.id.textView)).setText("Windows");
		return android;
	}
}