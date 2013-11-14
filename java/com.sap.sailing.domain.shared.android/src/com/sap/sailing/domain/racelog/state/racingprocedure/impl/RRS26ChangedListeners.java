package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;

public class RRS26ChangedListeners extends RacingProcedureChangedListeners<RRS26ChangedListener> implements
        RRS26ChangedListener {

    @Override
    public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
        for (RRS26ChangedListener listener : getListeners()) {
            listener.onStartmodeChanged(racingProcedure);
        }
    }

}
