package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.basic.BasicRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;

public class BasicStartphaseRaceFragment extends BaseStartphaseRaceFragment<BasicRacingProcedure> {

    public static BasicStartphaseRaceFragment newInstance() {
        return new BasicStartphaseRaceFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendIntent(AppConstants.ACTION_SHOW_MAIN_CONTENT);
    }
}
