package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public abstract class BaseRacingProcedureChangedListener implements RacingProcedureChangedListener {

    @Override
    public void onActiveFlagsChanged(RacingProcedure racingProcedure) {

    }

    @Override
    public void onIndividualRecallDisplayed(RacingProcedure racingProcedure) {

    }

    @Override
    public void onIndividualRecallRemoved(RacingProcedure racingProcedure) {

    }

}
