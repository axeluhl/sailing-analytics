package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;

public interface GateStartPhaseEventListener extends StartPhaseEventListener{
    
    void onClassOverGolfUp();
    
    void onPapaUp();
    
    void onPapaDown();
    
}
