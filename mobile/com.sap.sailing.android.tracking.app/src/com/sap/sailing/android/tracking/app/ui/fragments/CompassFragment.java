package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class CompassFragment extends BaseFragment {
	
	private String TAG = CompassFragment.class.getName(); 

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_compass, container, false);		
		return view;
	}

	public void setBearing(float heading)
	{
		TextView bearingTextView = (TextView) getActivity().findViewById(R.id.compass_bearing_text_view);
		bearingTextView.setText(String.valueOf(Math.round(heading)) + "°");
	}
}
