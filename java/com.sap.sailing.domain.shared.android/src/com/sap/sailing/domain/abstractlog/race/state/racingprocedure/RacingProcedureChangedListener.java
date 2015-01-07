package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

public interface RacingProcedureChangedListener {
    
    void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure);
    void onIndividualRecallDisplayed(ReadonlyRacingProcedure racingProcedure);
    void onIndividualRecallRemoved(ReadonlyRacingProcedure racingProcedure);

}
