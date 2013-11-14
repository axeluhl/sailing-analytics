package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;

public class GateStartChangedListeners extends RacingProcedureChangedListeners<GateStartChangedListener> implements
        GateStartChangedListener {

    @Override
    public void onGateLineOpeningTimeChanged(GateStartRacingProcedure gateStartRacingProcedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onGateLineOpeningTimeChanged(gateStartRacingProcedure);
        }
    }

    @Override
    public void onPathfinderChanged(GateStartRacingProcedure procedure) {
        for (GateStartChangedListener listener : getListeners()) {
            listener.onPathfinderChanged(procedure);
        }
    }


}
