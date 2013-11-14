package com.sap.sailing.domain.racelog.state.racingprocedure;

public interface GateStartChangedListener extends RacingProcedureChangedListener {
    
    void onGateLineOpeningTimeChanged(GateStartRacingProcedure procedure);
    void onPathfinderChanged(GateStartRacingProcedure procedure);

}
