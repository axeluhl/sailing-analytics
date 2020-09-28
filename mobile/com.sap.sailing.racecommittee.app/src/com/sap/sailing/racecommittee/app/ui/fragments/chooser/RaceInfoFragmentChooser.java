package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import android.content.Context;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.ErrorRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartTimeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished.BasicFinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.BasicStartphaseRaceFragment;

public class RaceInfoFragmentChooser {
    private static final String TAG = RaceInfoFragmentChooser.class.getName();

    private RaceInfoFragmentChooser() {
    }

    public static RaceFragment choose(Context context, ManagedRace managedRace) {
        switch (managedRace.getStatus()) {
            case UNSCHEDULED:
                return createInfoFragment(StartTimeFragment.newInstance(StartTimeFragment.START_MODE_PRESETUP), managedRace);
            case PRESCHEDULED:
            case SCHEDULED:
            case STARTPHASE:
            case RUNNING:
            case FINISHING:
                return createInfoFragment(context, BasicStartphaseRaceFragment.class, managedRace);
            case FINISHED:
                return createInfoFragment(context, BasicFinishedRaceFragment.class, managedRace);
            default:
                return createInfoFragment(context, ErrorRaceFragment.class, managedRace);
        }
    }

    private static RaceFragment createInfoFragment(
            Context context, Class<? extends RaceFragment> fragmentClass,
            ManagedRace managedRace
    ) {
        try {
            RaceFragment fragment = fragmentClass.newInstance();
            return createInfoFragment(fragment, managedRace);
        } catch (Exception e) {
            ExLog.e(context, TAG, String.format("Exception while instantiating race info fragment:\n%s", e.toString()));
            return new ErrorRaceFragment();
        }
    }

    private static RaceFragment createInfoFragment(RaceFragment fragment, ManagedRace managedRace) {
        fragment.setArguments(RaceFragment.createArguments(managedRace));
        return fragment;
    }
}
