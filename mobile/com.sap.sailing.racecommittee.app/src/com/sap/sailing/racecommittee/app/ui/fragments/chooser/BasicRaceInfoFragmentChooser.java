package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.BasicFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing.BasicFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.BasicRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.BasicStartphaseRaceFragment;

public class BasicRaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return BasicStartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return BasicStartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return BasicStartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return BasicFinishedRaceFragment.class;
    }

}
