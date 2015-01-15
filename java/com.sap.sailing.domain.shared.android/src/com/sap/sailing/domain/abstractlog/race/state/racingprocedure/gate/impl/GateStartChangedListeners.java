package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;

public class GateStartChangedListeners extends RacingProcedureChangedListeners<GateStartChangedListener> implements
        GateStartChangedListener {

    @Override
    public void onGateLaunchTimeChanged(ReadonlyGateStartRacingProcedure gateStartRacingProcedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onGateLaunchTimeChanged(gateStartRacingProcedure);
        }
    }

    @Override
    public void onPathfinderChanged(ReadonlyGateStartRacingProcedure procedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onPathfinderChanged(procedure);
        }
    }


}
