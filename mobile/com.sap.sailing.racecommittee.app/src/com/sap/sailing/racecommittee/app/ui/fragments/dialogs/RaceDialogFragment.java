package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public abstract class RaceDialogFragment extends LoggableDialogFragment implements TickListener {

    public static Bundle createArguments(ManagedRace race) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(AppConstants.RACE_ID_KEY, race.getId());
        return arguments;
    }

    private ManagedRace managedRace;
    protected AppPreferences preferences;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        preferences = AppPreferences.on(activity);
    }

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

    public ManagedRace getRace() {
        return managedRace;
    }
    
    public RaceState getRaceState() {
        return getRace().getState();
    }
    
    @Override
    public void notifyTick() {
        // see subclasses
    }

    /**
     * Creates a bundle that contains the race id as parameter for the next fragment
     * 
     * @return a bundle containing the race id
     */
    protected Bundle getParameterBundle() {
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.RACE_ID_KEY, managedRace.getId());
        return args;
    }
}
