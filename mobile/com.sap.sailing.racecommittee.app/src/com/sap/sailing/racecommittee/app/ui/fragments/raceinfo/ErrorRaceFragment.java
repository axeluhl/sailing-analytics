package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

import android.os.Bundle;

public class ErrorRaceFragment extends RaceFragment {
    private static final String TAG = ErrorRaceFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExLog.e(getActivity(), TAG, "Somehow the error race fragment got selected...");
    }

}
