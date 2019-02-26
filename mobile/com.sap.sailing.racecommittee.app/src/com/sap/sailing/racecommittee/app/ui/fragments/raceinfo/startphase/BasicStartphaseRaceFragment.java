package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.basic.BasicRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;

import android.os.Bundle;

public class BasicStartphaseRaceFragment extends BaseStartphaseRaceFragment<BasicRacingProcedure> {

    public static BasicStartphaseRaceFragment newInstance() {
        BasicStartphaseRaceFragment fragment = new BasicStartphaseRaceFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
    }
}
