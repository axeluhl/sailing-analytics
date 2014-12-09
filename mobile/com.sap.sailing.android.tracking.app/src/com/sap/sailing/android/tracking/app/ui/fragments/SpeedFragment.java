package com.sap.sailing.android.tracking.app.ui.fragments;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class SpeedFragment extends BaseFragment {

	private String TAG = SpeedFragment.class.getName(); 
	private final String SIS_SPEED_TEXTIVEW = "SpeedFragmentSpeedTextView"; 

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_speed, container, false);		
		return view;
	}

	public void setSpeed(float speedInMetersPerSecond)
	{
		if (isAdded())
		{
			float speedInKnots = speedInMetersPerSecond * 1.9438444924574f;

			NumberFormat df = DecimalFormat.getInstance();
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(2);
			df.setRoundingMode(RoundingMode.HALF_UP);
			String formattedSpeed = df.format(speedInKnots);
			
			TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);
			speedText.setText(formattedSpeed + "kn");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TextView speedText = (TextView)getActivity().findViewById(R.id.speed_text_view);
		if (savedInstanceState != null)
		{
			speedText.setText(savedInstanceState.getString(SIS_SPEED_TEXTIVEW));
		}
		else
		{
			speedText.setText("--kn");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		TextView speedText = (TextView)getActivity().findViewById(R.id.speed_text_view);
		outState.putString(SIS_SPEED_TEXTIVEW, speedText.getText().toString());
	}
}
