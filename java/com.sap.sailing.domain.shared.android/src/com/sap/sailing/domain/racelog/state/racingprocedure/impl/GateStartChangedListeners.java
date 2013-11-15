package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;

public class GateStartChangedListeners extends RacingProcedureChangedListeners<GateStartChangedListener> implements
        GateStartChangedListener {

    @Override
    public void onGateLaunchTimeChanged(GateStartRacingProcedure gateStartRacingProcedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onGateLaunchTimeChanged(gateStartRacingProcedure);
        }
    }

    @Override
    public void onPathfinderChanged(GateStartRacingProcedure procedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onPathfinderChanged(procedure);
        }
    }


}
