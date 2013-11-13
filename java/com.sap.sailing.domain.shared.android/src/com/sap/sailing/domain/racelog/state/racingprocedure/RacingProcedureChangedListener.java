package com.sap.sailing.domain.racelog.state.racingprocedure;

public interface RacingProcedureChangedListener {
    
    void onActiveFlagsChanged(RacingProcedure2 racingProcedure);
    void onIndividualRecallDisplayed(RacingProcedure2 racingProcedure);
    void onIndividualRecallRemoved(RacingProcedure2 racingProcedure);

}
