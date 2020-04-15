package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.ErrorRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartTimeFragment;

import android.content.Context;

public abstract class RaceInfoFragmentChooser {
    private static final String TAG = RaceInfoFragmentChooser.class.getName();

    public static RaceInfoFragmentChooser on(RacingProcedureType racingProcedureType) {
        return new BasicRaceInfoFragmentChooser();
    }

    protected abstract Class<? extends RaceFragment> getStartphaseFragment();

    protected abstract Class<? extends RaceFragment> getRunningFragment();

    protected abstract Class<? extends RaceFragment> getFinishingFragment();

    protected abstract Class<? extends RaceFragment> getFinishedFragment();

    public RaceFragment choose(Context context, ManagedRace managedRace) {
        switch (managedRace.getStatus()) {
        case UNSCHEDULED:
            return createInfoFragment(StartTimeFragment.newInstance(StartTimeFragment.START_MODE_PRESETUP),
                    managedRace);
        case PRESCHEDULED:
        case SCHEDULED:
        case STARTPHASE:
            return createInfoFragment(context, getStartphaseFragment(), managedRace);
        case RUNNING:
            return createInfoFragment(context, getRunningFragment(), managedRace);
        case FINISHING:
            return createInfoFragment(context, getFinishingFragment(), managedRace);
        case FINISHED:
            return createInfoFragment(context, getFinishedFragment(), managedRace);
        default:
            return createInfoFragment(context, ErrorRaceFragment.class, managedRace);
        }
    }

    protected RaceFragment createInfoFragment(Context context, Class<? extends RaceFragment> fragmentClass,
            ManagedRace managedRace) {
        try {
            RaceFragment fragment = fragmentClass.newInstance();
            return createInfoFragment(fragment, managedRace);
        } catch (Exception e) {
            ExLog.e(context, TAG, String.format("Exception while instantiating race info fragment:\n%s", e.toString()));
            return new ErrorRaceFragment();
        }
    }

    protected RaceFragment createInfoFragment(RaceFragment fragment, ManagedRace managedRace) {
        fragment.setArguments(RaceFragment.createArguments(managedRace));
        return fragment;
    }
}
