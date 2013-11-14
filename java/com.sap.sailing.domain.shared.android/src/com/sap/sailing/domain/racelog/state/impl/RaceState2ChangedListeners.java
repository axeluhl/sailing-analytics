package com.sap.sailing.domain.racelog.state.impl;

import java.util.HashSet;

import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;

public class RaceState2ChangedListeners extends HashSet<RaceStateChangedListener> implements RaceStateChangedListener {
    
    private static final long serialVersionUID = 2028183211903975659L;

    @Override
    public void onRacingProcedureChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onRacingProcedureChanged(state);
        }
    }

    @Override
    public void onStatusChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onStatusChanged(state);
        }
    }

    @Override
    public void onStartTimeChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onStartTimeChanged(state);
        }
    }

    @Override
    public void onFinishingTimeChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingTimeChanged(state);
        }
    }

    @Override
    public void onFinishedTimeChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishedTimeChanged(state);
        }
    }

    @Override
    public void onProtestTimeChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onProtestTimeChanged(state);
        }
    }

    @Override
    public void onAdvancePass(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onAdvancePass(state);
        }
    }

    @Override
    public void onFinishingPositioningsChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingPositioningsChanged(state);
        }
    }

    @Override
    public void onFinishingPositionsConfirmed(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingPositionsConfirmed(state);
        }
    }

    @Override
    public void onCourseDesignChanged(RaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onCourseDesignChanged(state);
        }
    }
    
    

}
