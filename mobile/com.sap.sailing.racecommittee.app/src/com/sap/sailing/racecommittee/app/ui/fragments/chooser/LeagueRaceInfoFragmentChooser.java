package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.LeagueFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing.LeagueFinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.LeagueRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.LeagueStartphaseRaceFragment;

public class LeagueRaceInfoFragmentChooser extends RaceInfoFragmentChooser {

    @Override
    protected Class<? extends RaceFragment> getStartphaseFragment() {
        return LeagueStartphaseRaceFragment.class;
    }
    
    @Override
    protected Class<? extends RaceFragment> getRunningFragment() {
        return LeagueRunningRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishingFragment() {
        return LeagueFinishingRaceFragment.class;
    }

    @Override
    protected Class<? extends RaceFragment> getFinishedFragment() {
        return LeagueFinishedRaceFragment.class;
    }

}
