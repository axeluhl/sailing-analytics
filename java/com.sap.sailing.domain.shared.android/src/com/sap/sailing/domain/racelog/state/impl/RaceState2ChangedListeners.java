package com.sap.sailing.domain.racelog.state.impl;

import java.util.HashSet;

import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.RaceState2ChangedListener;

public class RaceState2ChangedListeners extends HashSet<RaceState2ChangedListener> implements RaceState2ChangedListener {
    
    private static final long serialVersionUID = 2028183211903975659L;

    @Override
    public void onRacingProcedureChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onRacingProcedureChanged(state);
        }
    }

    @Override
    public void onStatusChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onStatusChanged(state);
        }
    }

    @Override
    public void onStartTimeChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onStartTimeChanged(state);
        }
    }

    @Override
    public void onFinishingTimeChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onFinishingTimeChanged(state);
        }
    }

    @Override
    public void onFinishedTimeChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onFinishedTimeChanged(state);
        }
    }

    @Override
    public void onProtestTimeChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onProtestTimeChanged(state);
        }
    }

    @Override
    public void onAdvancePass(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onAdvancePass(state);
        }
    }

    @Override
    public void onFinishingPositioningsChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onFinishingPositioningsChanged(state);
        }
    }

    @Override
    public void onFinishingPositionsConfirmed(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onFinishingPositionsConfirmed(state);
        }
    }

    @Override
    public void onCourseDesignChanged(RaceState2 state) {
        for (RaceState2ChangedListener listener : this) {
            listener.onCourseDesignChanged(state);
        }
    }
    
    

}
