package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.LineStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;

public class LineStartChangedListeners extends RacingProcedureChangedListeners<LineStartChangedListener> implements
        LineStartChangedListener {

    @Override
    public void onStartModeChanged(ConfigurableStartModeFlagRacingProcedure racingProcedure) {
        for (LineStartChangedListener listener : getListeners()) {
            listener.onStartModeChanged(racingProcedure);
        }
    }
}
