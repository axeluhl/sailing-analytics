package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;

public interface GateStartRunningRaceEventListener extends RunningRaceEventListener{
    
    void onGolfDown();
    
}
