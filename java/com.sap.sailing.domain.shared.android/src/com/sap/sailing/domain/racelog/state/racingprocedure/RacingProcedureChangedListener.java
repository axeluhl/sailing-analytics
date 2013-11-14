package com.sap.sailing.domain.racelog.state.racingprocedure;

public interface RacingProcedureChangedListener {
    
    void onActiveFlagsChanged(RacingProcedure racingProcedure);
    void onIndividualRecallDisplayed(RacingProcedure racingProcedure);
    void onIndividualRecallRemoved(RacingProcedure racingProcedure);

}
