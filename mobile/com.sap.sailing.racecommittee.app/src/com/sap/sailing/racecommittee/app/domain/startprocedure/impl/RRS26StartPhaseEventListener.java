package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;

public interface RRS26StartPhaseEventListener extends StartPhaseEventListener{
    
    void onClassUp();
    
    void onStartModeUp();
    
    void onStartModeDown();
    
    void onClassDown();
}
