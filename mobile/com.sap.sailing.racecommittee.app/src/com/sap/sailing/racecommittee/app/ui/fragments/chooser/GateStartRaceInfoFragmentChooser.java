package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.GateStartFinishedFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing.GateStartFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.GateStartRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.GateStartStartphaseRaceFragment;

public class GateStartRaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return GateStartStartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return GateStartRunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return GateStartFinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return GateStartFinishedFragment.class;
    }

}
