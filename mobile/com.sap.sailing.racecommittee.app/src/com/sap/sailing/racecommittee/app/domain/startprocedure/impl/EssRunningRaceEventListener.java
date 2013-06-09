package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;

public interface EssRunningRaceEventListener extends RunningRaceEventListener{
    
    void onIndividualRecall();
    
    void onIndividualRecallRemoval();
}
