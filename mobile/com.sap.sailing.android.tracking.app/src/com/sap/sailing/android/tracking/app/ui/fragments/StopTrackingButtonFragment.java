package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

public class StopTrackingButtonFragment extends BaseFragment {
	
	private final static String SIS_TRACKING_TIMER = "savedInstanceTrackingTimer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_stop_button, container,
				false);

		Button stopTracking = (Button) view.findViewById(R.id.stop_tracking);
		stopTracking.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.stop_tracking:
					TrackingActivity activity = (TrackingActivity) getActivity();
					activity.showStopTrackingConfirmationDialog();
					break;
				default:
					break;
				}
			}
		});

		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		TextView timerView = (TextView) getActivity().findViewById(R.id.tracking_time_label);
		outState.putString(SIS_TRACKING_TIMER, timerView.getText().toString());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null)
		{
			TextView timerView = (TextView) getActivity().findViewById(R.id.tracking_time_label);
			timerView.setText(savedInstanceState.getString(SIS_TRACKING_TIMER));	
		}
	}
}
