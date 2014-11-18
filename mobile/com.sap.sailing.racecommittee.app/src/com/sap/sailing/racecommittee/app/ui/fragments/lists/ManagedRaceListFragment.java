package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;
import com.sap.sailing.domain.racelog.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.RaceApplication;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter.JuryFlagClickedListener;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeHeader;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;
import com.sap.sailing.racecommittee.app.ui.comparators.BoatClassSeriesDataFleetComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ProtestTimeDialogFragment;

public class ManagedRaceListFragment extends LoggableListFragment implements JuryFlagClickedListener {

    public enum FilterMode {
        ALL(R.string.race_list_filter_show_all), ACTIVE(R.string.race_list_filter_show_active);

        private String displayName;

        private FilterMode(int resId) {
            this.displayName = RaceApplication.getStringContext().getString(resId);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final String TAG = ManagedRaceListFragment.class.getName();

    private FilterMode filterMode;
    private ManagedRaceListAdapter adapter;
    private ManagedRace selectedRace;
    private HashMap<Serializable, ManagedRace> managedRacesById;
    private TreeMap<BoatClassSeriesFleet, List<ManagedRace>> racesByGroup;
    private ArrayList<RaceListDataType> viewItems;

    public ManagedRaceListFragment() {
        this.filterMode = FilterMode.ACTIVE;
        this.selectedRace = null;
        this.managedRacesById = new HashMap<Serializable, ManagedRace>();
        this.racesByGroup = new TreeMap<BoatClassSeriesFleet, List<ManagedRace>>(
                new BoatClassSeriesDataFleetComparator());
        this.viewItems = new ArrayList<RaceListDataType>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // pass the localized string to the data elements...
        RaceListDataTypeRace.initializeTemplates(this);

        this.adapter = new ManagedRaceListAdapter(getActivity(), viewItems, this);
        setListAdapter(adapter);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        unregisterOnAllRaces();
        registerOnAllRaces();
        for (ManagedRace race : managedRacesById.values()) {
            //onRaceStateStatusChanged(race.getState());
            stateListener.onStatusChanged(race.getState());
        }
    }

    @Override
    public void onStop() {
        unregisterOnAllRaces();
        super.onStop();
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        filterChanged();
    }

    public void setupOn(Collection<ManagedRace> races) {
        ExLog.i(getActivity(), TAG, String.format("Setting up %s with %d races.", this.getClass().getSimpleName(), races.size()));
        
        unregisterOnAllRaces();
        managedRacesById.clear();

        for (ManagedRace managedRace : races) {
            managedRacesById.put(managedRace.getId(), managedRace);
        }
        registerOnAllRaces();

        // prepare views and do initial filtering
        initializeViewElements();
        filterChanged();
        adapter.notifyDataSetChanged();
    }

    private void registerOnAllRaces() {
        for (ManagedRace managedRace : managedRacesById.values()) {
            //managedRace.getState().registerStateChangeListener(this);
            managedRace.getState().addChangedListener(stateListener);
        }
    }

    private void unregisterOnAllRaces() {
        for (ManagedRace managedRace : managedRacesById.values()) {
            //managedRace.getState().unregisterStateChangeListener(this);
            managedRace.getState().removeChangedListener(stateListener);
        }
    }

    private void initializeViewElements() {
        // 1. Group races by <boat class, series, fleet>
        initializeRacesByGroup();
        
        // 2. Remove previous view items
        viewItems.clear();

        // 3. Create view elements from tree
        for (BoatClassSeriesFleet key : racesByGroup.navigableKeySet()) {
            // ... add the header view...
            viewItems.add(new RaceListDataTypeHeader(key));

            List<ManagedRace> races = racesByGroup.get(key);
            Collections.sort(races, new NaturalNamedComparator());
            for (ManagedRace race : races) {
                // ... and add the race view!
                viewItems.add(new RaceListDataTypeRace(race));
            }
        }
    }

    private void initializeRacesByGroup() {
        racesByGroup.clear();
        for (ManagedRace race : managedRacesById.values()) {
            BoatClassSeriesFleet container = new BoatClassSeriesFleet(race);

            if (!racesByGroup.containsKey(container)) {
                racesByGroup.put(container, new LinkedList<ManagedRace>());
            }
            racesByGroup.get(container).add(race);
        }
    }

    private void filterChanged() {
        //adapter.getFilter().filterByMode(getFilterMode());
        adapter.notifyDataSetChanged();
    }

    private void dataChanged(ReadonlyRaceState changedState) {
        List<RaceListDataType> adapterItems = adapter.getItems();
        for (int i = 0; i < adapterItems.size(); ++i) {
            if (adapterItems.get(i) instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceView = (RaceListDataTypeRace) adapterItems.get(i);
                ManagedRace race = raceView.getRace();
                if (changedState != null && race.getState().equals(changedState)) {
                    boolean allowUpdateIndicator = !raceView.getRace().equals(this.selectedRace);
                    raceView.onStatusChanged(changedState.getStatus(), allowUpdateIndicator);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        RaceListDataType selectedItem = adapter.getItem(position);
        if (selectedItem instanceof RaceListDataTypeRace) {
            RaceListDataTypeRace selectedElement = (RaceListDataTypeRace) selectedItem;
            selectedElement.setUpdateIndicatorVisible(false);
            ((ImageView) view.findViewById(R.id.Welter_Cell_UpdateLabel)).setVisibility(View.GONE);

            selectedRace = selectedElement.getRace();
            ExLog.i(getActivity(), LogEvent.RACE_SELECTED_ELEMENT, selectedRace.getId() + " " + selectedRace.getStatus());
            ((RacingActivity) getActivity()).onRaceItemClicked(selectedRace);

        } else if (selectedItem instanceof RaceListDataTypeHeader) {
            // This is for logging purposes only!
            RaceListDataTypeHeader selectedTitle = (RaceListDataTypeHeader) selectedItem;
            ExLog.i(getActivity(), LogEvent.RACE_SELECTED_TITLE, selectedTitle.toString());
        }
    }

    @Override
    public void onJuryFlagClicked(BoatClassSeriesFleet group) {
        if (racesByGroup.containsKey(group)) {
            List<ManagedRace> races = racesByGroup.get(group);
            ProtestTimeDialogFragment fragment = ProtestTimeDialogFragment.newInstace(races);
            fragment.show(getFragmentManager(), null);
        }
    }
    
    private BaseRaceStateChangedListener stateListener = new BaseRaceStateChangedListener() {
        public void update(ReadonlyRaceState state) {
            dataChanged(state); 
            filterChanged();
        }
        
        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            update(state);
        };
        
        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            update(state);
        };
    };

}
