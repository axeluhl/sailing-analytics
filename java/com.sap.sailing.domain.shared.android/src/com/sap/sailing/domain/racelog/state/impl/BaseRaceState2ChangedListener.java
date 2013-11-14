package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;

public abstract class BaseRaceState2ChangedListener implements RaceStateChangedListener {

    @Override
    public void onRacingProcedureChanged(RaceState state) {

    }

    @Override
    public void onStatusChanged(RaceState state) {

    }

    @Override
    public void onStartTimeChanged(RaceState state) {

    }

    @Override
    public void onFinishingTimeChanged(RaceState state) {

    }

    @Override
    public void onFinishedTimeChanged(RaceState state) {

    }

    @Override
    public void onProtestTimeChanged(RaceState state) {

    }
    
    @Override
    public void onAdvancePass(RaceState state) {

    }

    @Override
    public void onFinishingPositioningsChanged(RaceState state) {

    }

    @Override
    public void onFinishingPositionsConfirmed(RaceState state) {

    }

    @Override
    public void onCourseDesignChanged(RaceState state) {

    }

}
