package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;

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
