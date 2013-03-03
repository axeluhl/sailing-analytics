package com.sap.sailing.racecommittee.app.services;

import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RaceStateListener implements RaceStateChangedListener {
    private final static String TAG = RaceStateListener.class.getName();
    
    private final ManagedRace race;
    
    public RaceStateListener(ManagedRace race) {
        this.race = race;
    }

    public void onRaceStateChanged(RaceState state) {
        ExLog.i(TAG, "Race state changed but service is ignoring it...");
    }

}
