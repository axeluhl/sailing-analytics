package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;

public interface RRS26StartPhaseEventListener extends StartPhaseEventListener{
    
    void onClassUp();
    
    void onStartModeUp(Flags startModeFlag);
    
    void onStartModeDown(Flags startModeFlag);
    
    void onStartModeFlagChosen(Flags startModeFlag);
    
    void onClassDown();
}
