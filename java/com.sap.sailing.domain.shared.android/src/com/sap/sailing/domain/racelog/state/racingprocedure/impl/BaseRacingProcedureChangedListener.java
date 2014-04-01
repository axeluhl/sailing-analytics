package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public abstract class BaseRacingProcedureChangedListener implements RacingProcedureChangedListener {

    @Override
    public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {

    }

    @Override
    public void onIndividualRecallDisplayed(ReadonlyRacingProcedure racingProcedure) {

    }

    @Override
    public void onIndividualRecallRemoved(ReadonlyRacingProcedure racingProcedure) {

    }

}
