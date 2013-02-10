package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;

import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class ErrorInfoFragment extends RaceFragment {
	private static final String TAG = ErrorInfoFragment.class.getName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ExLog.e(TAG, "Somehow the error race info fragment got selected...");
	}
	
}
