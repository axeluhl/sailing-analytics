package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class CompassFragment extends BaseFragment {
	
	private String TAG = CompassFragment.class.getName(); 
	private final String SIS_HEADING_TEXTIVEW = "CompassFragmentHeadingTextView"; 

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
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null)
		{
			TextView headingText = (TextView)getActivity().findViewById(R.id.compass_bearing_header_text_view);
			headingText.setText(savedInstanceState.getString(SIS_HEADING_TEXTIVEW));
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		TextView headingText = (TextView)getActivity().findViewById(R.id.compass_bearing_header_text_view);
		outState.putString(SIS_HEADING_TEXTIVEW, headingText.getText().toString());
	}
	
}
