package com.sap.sailing.domain.racelog.state.racingprocedure;

public interface GateStartChangedListener extends RacingProcedureChangedListener {
    
    void onGateLaunchTimeChanged(GateStartRacingProcedure procedure);
    void onPathfinderChanged(GateStartRacingProcedure procedure);

}
