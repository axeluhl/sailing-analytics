package com.sap.sailing.domain.abstractlog.race.state.impl;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;

/**
 * Providing an empty implementation of {@link RaceStateChangedListener}.
 */
public abstract class BaseRaceStateChangedListener implements RaceStateChangedListener {

    @Override
    public void onRacingProcedureChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onStatusChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onStartTimeChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onFinishingTimeChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onFinishedTimeChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onProtestTimeChanged(ReadonlyRaceState state) {

    }
    
    @Override
    public void onAdvancePass(ReadonlyRaceState state) {

    }

    @Override
    public void onFinishingPositioningsChanged(ReadonlyRaceState state) {

    }

    @Override
    public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {

    }

    @Override
    public void onCourseDesignChanged(ReadonlyRaceState state) {

    }
    
    @Override
    public void onWindFixChanged(ReadonlyRaceState state) {
        
    }

    @Override
    public void onTagEventsChanged(ReadonlyRaceState state) {
        
    }

}
