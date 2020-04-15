package com.sap.sailing.domain.abstractlog.race.state.impl;

import java.util.HashSet;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;

public class RaceStateChangedListeners extends HashSet<RaceStateChangedListener> implements RaceStateChangedListener {
    
    private static final long serialVersionUID = 2028183211903975659L;

    @Override
    public boolean add(RaceStateChangedListener listener) {
        synchronized (this) {
            return super.add(listener);
        }
    }
    
    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            return super.remove(o);
        }
    }

    /**
     * Produces a working copy that can safely be iterated over. While producing, holds this objects
     * monitor using {@code synchronized(this)} which teams with {@link #add(RaceStateChangedListener)} and
     * {@link #remove} which
     * also synchronized on {@code this} object.
     */
    private Iterable<RaceStateChangedListener> getWorkingCopyOfListeners() {
        synchronized (this) {
            return new HashSet<>(this);
        }
    }
    
    @Override
    public void onRacingProcedureChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onRacingProcedureChanged(state);
        }
    }

    @Override
    public void onStatusChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onStatusChanged(state);
        }
    }

    @Override
    public void onStartTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onStartTimeChanged(state);
        }
    }

    @Override
    public void onFinishingTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onFinishingTimeChanged(state);
        }
    }

    @Override
    public void onFinishedTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onFinishedTimeChanged(state);
        }
    }

    @Override
    public void onProtestTimeChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onProtestTimeChanged(state);
        }
    }

    @Override
    public void onAdvancePass(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onAdvancePass(state);
        }
    }

    @Override
    public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onFinishingPositioningsChanged(state);
        }
    }

    @Override
    public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onFinishingPositionsConfirmed(state);
        }
    }

    @Override
    public void onCourseDesignChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onCourseDesignChanged(state);
        }
    }

    @Override
    public void onWindFixChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onWindFixChanged(state);
        }
    }

    @Override
    public void onTagEventsChanged(ReadonlyRaceState state) {
        for (RaceStateChangedListener listener : getWorkingCopyOfListeners()) {
            listener.onTagEventsChanged(state);
        }
    }
}
