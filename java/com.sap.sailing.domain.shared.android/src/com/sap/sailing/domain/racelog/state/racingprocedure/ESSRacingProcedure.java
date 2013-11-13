package com.sap.sailing.domain.racelog.state.racingprocedure;


public interface ESSRacingProcedure extends RacingProcedure2 {
    
    void addChangedListener(ESSChangedListener listener);
    void removeChangedListener(ESSChangedListener listener);

}
