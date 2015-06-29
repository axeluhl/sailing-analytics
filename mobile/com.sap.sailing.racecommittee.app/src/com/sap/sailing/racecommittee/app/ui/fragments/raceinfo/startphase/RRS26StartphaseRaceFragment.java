package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;

public class RRS26StartphaseRaceFragment extends BaseStartphaseRaceFragment<RRS26RacingProcedure> {

    public RRS26StartphaseRaceFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void setupUi() {
        // TODO: Maybe check for something like getRacingProcedure().isStartmodeFlagUp()
        super.setupUi();
    }
}
