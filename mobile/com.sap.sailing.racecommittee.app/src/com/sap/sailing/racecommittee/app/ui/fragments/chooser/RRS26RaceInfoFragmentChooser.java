package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RRS26RunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.RRS26StartphaseRaceFragment2;

public class RRS26RaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return RRS26StartphaseRaceFragment2.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return RRS26RunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return FinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return FinishedRaceFragment.class;
    }

}
