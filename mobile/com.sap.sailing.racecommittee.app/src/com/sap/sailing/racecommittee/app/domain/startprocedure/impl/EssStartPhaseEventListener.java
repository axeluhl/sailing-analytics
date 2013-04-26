package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;

public interface EssStartPhaseEventListener extends StartPhaseEventListener{
    
    void onAPDown();
    
    void onEssThreeUp();
    
    void onEssTwoUp();
    
    void onEssOneUp();
}
