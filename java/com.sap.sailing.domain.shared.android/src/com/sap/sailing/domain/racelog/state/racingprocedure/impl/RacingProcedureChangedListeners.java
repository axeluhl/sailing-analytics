package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.HashSet;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public class RacingProcedureChangedListeners<T extends RacingProcedureChangedListener> extends HashSet<T> implements
        RacingProcedureChangedListener {

    private static final long serialVersionUID = 3518707638312002482L;
    
    /**
     * Adds a {@link RacingProcedureChangedListener} to this set. The type is checked on runtime!
     */
    @SuppressWarnings("unchecked")
    public boolean addListener(Object listener) {
        if (listener instanceof RacingProcedureChangedListener) {
            return super.add((T)listener);
        } else {
            throw new IllegalArgumentException("listener");
        }
    }

    @Override
    public void onActiveFlagsChanged(RacingProcedure2 racingProcedure) {
        for (RacingProcedureChangedListener listener : this) {
            listener.onActiveFlagsChanged(racingProcedure);
        }
    }
    
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
