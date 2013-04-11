package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;

import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class ErrorRaceFragment extends RaceFragment {
    private static final String TAG = ErrorRaceFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExLog.e(TAG, "Somehow the error race fragment got selected...");
    }

    @Override
    public void notifyTick() {
        // TODO Auto-generated method stub
        
    }

}
