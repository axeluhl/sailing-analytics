package com.sap.sailing.domain.abstractlog.race.state.impl;

import java.util.HashSet;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;

public class RaceStateChangedListeners extends HashSet<RaceStateChangedListener> implements RaceStateChangedListener {
    
    private static final long serialVersionUID = 2028183211903975659L;

    @Override
    public void onRacingProcedureChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onRacingProcedureChanged(state);
        }
    }

    @Override
    public void onStatusChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onStatusChanged(state);
        }
    }

    @Override
    public void onStartTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onStartTimeChanged(state);
        }
    }

    @Override
    public void onFinishingTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingTimeChanged(state);
        }
    }

    @Override
    public void onFinishedTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishedTimeChanged(state);
        }
    }

    @Override
    public void onProtestTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onProtestTimeChanged(state);
        }
    }

    @Override
    public void onAdvancePass(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onAdvancePass(state);
        }
    }

    @Override
    public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingPositioningsChanged(state);
        }
    }

    @Override
    public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onFinishingPositionsConfirmed(state);
        }
    }

    @Override
    public void onCourseDesignChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : this) {
            listener.onCourseDesignChanged(state);
        }
    }

    @Override
    public void onWindFixChanged(ReadonlyRaceState state) {
        // TODO Auto-generated method stub
        
    }
    
    

}
