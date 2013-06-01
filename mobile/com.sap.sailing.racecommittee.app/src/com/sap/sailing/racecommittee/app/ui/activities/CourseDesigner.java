package com.sap.sailing.racecommittee.app.ui.activities;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.sap.sailing.racecommittee.app.R;

public class CourseDesigner extends BaseActivity {

	@SuppressWarnings("unused")
    private GoogleMap courseAreaMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_designer);


		courseAreaMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();
	}
}
