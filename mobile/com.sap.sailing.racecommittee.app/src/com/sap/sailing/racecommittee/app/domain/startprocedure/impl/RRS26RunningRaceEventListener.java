package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;

public interface RRS26RunningRaceEventListener extends RunningRaceEventListener{
    
    void onIndividualRecall();
    
    void onIndividualRecallRemoval();
}
