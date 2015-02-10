package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public abstract class RaceFragment extends LoggableFragment implements TickListener {

    private static final String TAG = RaceFragment.class.getName();

    public static Bundle createArguments(ManagedRace race) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(AppConstants.RACE_ID_KEY, race.getId());
        return arguments;
    }

    protected ManagedRace managedRace;

    protected AppPreferences preferences;
    public String fill2(int value) {
        String erg = String.valueOf(value);

        if (erg.length() < 2) {
            erg = "0" + erg;
        }
        return erg;
    }

    public String getDuration(Date date1, Date date2) {
        TimeUnit timeUnit = TimeUnit.SECONDS;

        long diffInMilli = date2.getTime() - date1.getTime();
        long s = timeUnit.convert(diffInMilli, TimeUnit.MILLISECONDS);

        long days = s / (24 * 60 * 60);
        long rest = s - (days * 24 * 60 * 60);
        long hrs = rest / (60 * 60);
        long rest1 = rest - (hrs * 60 * 60);
        long min = rest1 / 60;
        long sec = s % 60;

        String dates = "";
        if (days < 0 || hrs < 0 || min < 0 || sec < 0) {
            dates += "-";
            if (days < 0) {
                days *= -1;
            }
            if (hrs < 0) {
                hrs *= -1;
            }
            if (min < 0) {
                min *= -1;
            }
            if (sec < 0) {
                sec *= -1;
            }
        }
        if (days != 0) {
            dates = days + ":";
        }

        dates += fill2((int) hrs) + ":";
        dates += fill2((int) min) + ":";
        dates += fill2((int) sec);

        return dates;
    }

    public ManagedRace getRace() {
        return managedRace;
    }

    public RaceState getRaceState() {
        return getRace().getState();
    }

    /**
     * Creates a bundle that contains the race id as parameter for the next fragment
     * 
     * @return a bundle containing the race id
     */
    protected Bundle getRecentArguments() {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.RACE_ID_KEY, managedRace.getId());
        return args;
    }

    @Override
    public void notifyTick() {
        // see subclasses.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            Serializable raceId = getArguments().getSerializable(AppConstants.RACE_ID_KEY);
            managedRace = OnlineDataManager.create(getActivity()).getDataStore().getRace(raceId);
            if (managedRace == null) {
                throw new IllegalStateException(String.format("Unable to obtain ManagedRace from datastore on start of race fragment."));
            }
        } else {
            ExLog.i(getActivity(), TAG, "no arguments!?");
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        preferences = AppPreferences.on(activity);
    }

    @Override
    public void onStart() {
        super.onStart();

        TickSingleton.INSTANCE.registerListener(this);
        notifyTick();
    }

    @Override
    public void onStop() {
        super.onStop();

        TickSingleton.INSTANCE.unregisterListener(this);
    }
}
