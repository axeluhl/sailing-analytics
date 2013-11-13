package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EssRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.ESSStartphaseRaceFragment2;

public class ESSRaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return ESSStartphaseRaceFragment2.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return EssRunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return EssFinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return EssFinishedRaceFragment.class;
    }

}
