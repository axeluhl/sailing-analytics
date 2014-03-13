package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.ESSFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing.ESSFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.ESSRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.ESSStartphaseRaceFragment;

public class ESSRaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return ESSStartphaseRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return ESSRunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return ESSFinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return ESSFinishedRaceFragment.class;
    }

}
