package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;

public interface GateStartChangedListener extends RacingProcedureChangedListener {
    
    void onGateLaunchTimeChanged(ReadonlyGateStartRacingProcedure procedure);
    void onPathfinderChanged(ReadonlyGateStartRacingProcedure procedure);

}
