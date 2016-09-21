package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.ReadonlySWCRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.SWCChangedListener;

public class SWCChangedListeners extends RacingProcedureChangedListeners<SWCChangedListener> implements
        SWCChangedListener {

    @Override
    public void onStartmodeChanged(ReadonlySWCRacingProcedure racingProcedure) {
        for (SWCChangedListener listener : getListeners()) {
            listener.onStartmodeChanged(racingProcedure);
        }
    }

}
