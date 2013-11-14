package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;

public class GateStartChangedListeners extends RacingProcedureChangedListeners<GateStartChangedListener> implements
        GateStartChangedListener {

    private static final long serialVersionUID = 9128555669971858138L;

    @Override
    public void onGateLineOpeningTimeChanged(GateStartRacingProcedure gateStartRacingProcedure) {
        for (GateStartChangedListener listener : this) {
            listener.onGateLineOpeningTimeChanged(gateStartRacingProcedure);
        }
    }


}
