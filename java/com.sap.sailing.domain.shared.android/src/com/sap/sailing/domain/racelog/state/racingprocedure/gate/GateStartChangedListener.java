package com.sap.sailing.domain.racelog.state.racingprocedure.gate;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public interface GateStartChangedListener extends RacingProcedureChangedListener {
    
    void onGateLaunchTimeChanged(ReadonlyGateStartRacingProcedure procedure);
    void onPathfinderChanged(ReadonlyGateStartRacingProcedure procedure);

}
