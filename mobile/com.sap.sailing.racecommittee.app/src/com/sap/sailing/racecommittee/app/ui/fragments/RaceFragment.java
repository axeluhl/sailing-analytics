package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.Serializable;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

import android.app.Fragment;
import android.os.Bundle;

public abstract class RaceFragment extends Fragment implements TickListener {
    
    private static final String TAG = RaceFragment.class.getName();

    public static Bundle createArguments(ManagedRace race) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(AppConstants.RACE_ID_KEY, race.getId());
        return arguments;
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

    private ManagedRace managedRace;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Serializable raceId = getArguments().getSerializable(AppConstants.RACE_ID_KEY);
        managedRace = OnlineDataManager.create(getActivity()).getDataStore().getRace(raceId);
        if (managedRace == null) {
            throw new IllegalStateException(
                    String.format("Unable to obtain ManagedRace from datastore on start of race fragment."));
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExLog.i(TAG, String.format("Starting RaceFragment %s", this.getClass().getSimpleName()));
    }
    
    @Override
    public void onStart() {
        super.onStart();
        ExLog.i(TAG, String.format("Starting RaceFragment %s", this.getClass().getSimpleName()));
        TickSingleton.INSTANCE.registerListener(this);
        notifyTick();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ExLog.i(TAG, String.format("Resuming RaceFragment %s", this.getClass().getSimpleName()));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        ExLog.i(TAG, String.format("Pausing RaceFragment %s", this.getClass().getSimpleName()));
    }

    @Override
    public void onStop() {
        super.onStop();
        ExLog.i(TAG, String.format("Stopping RaceFragment %s", this.getClass().getSimpleName()));
        TickSingleton.INSTANCE.unregisterListener(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        ExLog.i(TAG, String.format("Destroying RaceFragment %s", this.getClass().getSimpleName()));
    }

    public ManagedRace getRace() {
        return managedRace;
    }
    
    @Override
    public void notifyTick() {
        // may be overriden in subclasses.
    }

}
