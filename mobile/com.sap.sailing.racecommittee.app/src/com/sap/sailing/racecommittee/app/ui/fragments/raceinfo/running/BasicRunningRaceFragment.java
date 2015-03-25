package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import android.os.Bundle;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.basic.BasicRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceFlagViewerFragment;

public class BasicRunningRaceFragment extends BaseRunningRaceFragment<BasicRacingProcedure> {

    public static BasicRunningRaceFragment newInstance() {
        BasicRunningRaceFragment fragment = new BasicRunningRaceFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
    }
}
