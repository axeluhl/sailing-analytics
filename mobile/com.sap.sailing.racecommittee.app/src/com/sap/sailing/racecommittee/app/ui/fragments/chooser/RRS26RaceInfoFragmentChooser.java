package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.RRS26FinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing.RRS26FinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.RRS26RunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.RRS26StartphaseRaceFragment;

public class RRS26RaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return RRS26StartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return RRS26RunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return RRS26FinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return RRS26FinishedRaceFragment.class;
    }

}
