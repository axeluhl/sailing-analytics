package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.HashSet;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public class RacingProcedureChangedListeners<T extends RacingProcedureChangedListener> extends HashSet<T> implements
        RacingProcedureChangedListener {

    private static final long serialVersionUID = 3518707638312002482L;

    @Override
    public void onIndividualRecallDisplayed(RacingProcedure2 racingProcedure) {
        for (RacingProcedureChangedListener listener : this) {
            listener.onIndividualRecallDisplayed(racingProcedure);
        }
    }

    @Override
    public void onIndividualRecallRemoved(RacingProcedure2 racingProcedure) {
        for (RacingProcedureChangedListener listener : this) {
            listener.onIndividualRecallRemoved(racingProcedure);
        }
    }

}
