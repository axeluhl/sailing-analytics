package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26ChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;

public class RRS26ChangedListeners extends RacingProcedureChangedListeners<RRS26ChangedListener> implements
        RRS26ChangedListener {

    @Override
    public void onStartmodeChanged(ReadonlyRRS26RacingProcedure racingProcedure) {
        for (RRS26ChangedListener listener : getListeners()) {
            listener.onStartmodeChanged(racingProcedure);
        }
    }

}
