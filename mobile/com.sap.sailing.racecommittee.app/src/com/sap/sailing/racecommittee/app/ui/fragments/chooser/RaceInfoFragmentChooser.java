package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.ErrorRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetTimeRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartphaseRaceFragment;

public class RaceInfoFragmentChooser {
    private static final String TAG = RaceInfoFragmentChooser.class.getName();

    public RaceFragment choose(ManagedRace managedRace) {
        switch (managedRace.getStatus()) {
        case UNSCHEDULED:
            return createInfoFragment(SetTimeRaceFragment.class, managedRace);
        case SCHEDULED:
        case STARTPHASE:
            return createInfoFragment(StartphaseRaceFragment.class, managedRace);
        case RUNNING:
            return createInfoFragment(RunningRaceFragment.class, managedRace);
        case FINISHING:
            return createInfoFragment(FinishingRaceFragment.class, managedRace);
        case FINISHED:
            return createInfoFragment(FinishedRaceFragment.class, managedRace);
        default:
            return createInfoFragment(ErrorRaceFragment.class, managedRace);
        }
    }

    private RaceFragment createInfoFragment(Class<? extends RaceFragment> fragmentClass, ManagedRace managedRace) {
        try {
            RaceFragment fragment = fragmentClass.newInstance();
            fragment.setArguments(RaceFragment.createArguments(managedRace));
            return fragment;
        } catch (Exception e) {
            ExLog.e(TAG, String.format("Exception while instantiating race info fragment:\n%s", e.toString()));
            return new ErrorRaceFragment();
        }
    }

}
