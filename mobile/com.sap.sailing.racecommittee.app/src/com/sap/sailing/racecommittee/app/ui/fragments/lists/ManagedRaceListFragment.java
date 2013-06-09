package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.BoatClassSeriesDataFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.ManagedRaceListAdapter.JuryFlagClickedListener;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeHeader;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeRace;
import com.sap.sailing.racecommittee.app.ui.comparators.BoatClassSeriesDataFleetComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;

public class ManagedRaceListFragment extends ListFragment implements JuryFlagClickedListener, RaceStateChangedListener {

    public interface ProtestTimeRequestedListener {
        public void onProtestTimeRequested(List<ManagedRace> races);
    }
    
    public enum FilterMode {
        ALL("Show all"), ACTIVE("Show active");

        private String displayName;

        private FilterMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private ProtestTimeRequestedListener hostActivity;

    private FilterMode filterMode;
    private ManagedRaceListAdapter adapter;
    private ManagedRace selectedRace;
    private HashMap<Serializable, ManagedRace> managedRacesById;
    private TreeMap<BoatClassSeriesDataFleet, List<ManagedRace>> racesByGroup;
    private ArrayList<RaceListDataType> viewItems;

    public ManagedRaceListFragment() {
        this.filterMode = FilterMode.ACTIVE;
        this.selectedRace = null;
        this.managedRacesById = new HashMap<Serializable, ManagedRace>();
        this.racesByGroup = new TreeMap<BoatClassSeriesDataFleet, List<ManagedRace>>(
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof ProtestTimeRequestedListener) {
            hostActivity = (ProtestTimeRequestedListener) activity;
        } else {
            throw new IllegalStateException(String.format(
                    "Instance of %s must be attached to instances of %s. Tried to attach to %s.",
                    ManagedRaceListFragment.class.getName(), ProtestTimeRequestedListener.class.getName(), activity
                            .getClass().getName()));
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        unregisterOnAllRaces();
        registerOnAllRaces();
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
            managedRace.getState().registerStateChangeListener(this);
        }
    }

    private void unregisterOnAllRaces() {
        for (ManagedRace managedRace : managedRacesById.values()) {
            managedRace.getState().unregisterStateChangeListener(this);
        }
    }

    private BoatClass getBoatClassForRace(ManagedRace managedRace) {
        if (managedRace.getRaceGroup().getBoatClass() == null) {
            return new BoatClassImpl(managedRace.getRaceGroup().getName(), false);
        }
        return managedRace.getRaceGroup().getBoatClass();
    }

    private void initializeViewElements() {
        // 1. Group races by <boat class, series, fleet>
        initializeRacesByGroup();

        // 2. Create view elements from tree
        for (BoatClassSeriesDataFleet key : racesByGroup.navigableKeySet()) {
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
            BoatClassSeriesDataFleet container = new BoatClassSeriesDataFleet(getBoatClassForRace(race),
                    race.getSeries(), race.getFleet());

            if (!racesByGroup.containsKey(container)) {
                racesByGroup.put(container, new LinkedList<ManagedRace>());
            }
            racesByGroup.get(container).add(race);
        }
    }

    private void filterChanged() {
        adapter.getFilter().filterByMode(getFilterMode());
        adapter.notifyDataSetChanged();
    }

    private void dataChanged(RaceState changedState) {
        List<RaceListDataType> adapterItems = adapter.getItems();
        for (int i = 0; i < adapterItems.size(); ++i) {
            if (adapterItems.get(i) instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceView = (RaceListDataTypeRace) adapterItems.get(i);
                ManagedRace race = raceView.getRace();
                if (changedState == null
                        || (race.getState().equals(changedState) && !raceView.getRace().equals(this.selectedRace))) {
                    raceView.setUpdateIndicator(true);
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
            selectedElement.setUpdateIndicator(false);
            ((ImageView) view.findViewById(R.id.Welter_Cell_UpdateLabel)).setVisibility(View.GONE);

            selectedRace = selectedElement.getRace();
            ExLog.i(ExLog.RACE_SELECTED_ELEMENT, selectedRace.getId() + " " + selectedRace.getStatus(), getActivity());
            ((RacingActivity) getActivity()).onRaceItemClicked(selectedRace);

        } else if (selectedItem instanceof RaceListDataTypeHeader) {
            // This is for logging purposes only!
            RaceListDataTypeHeader selectedTitle = (RaceListDataTypeHeader) selectedItem;
            ExLog.i(ExLog.RACE_SELECTED_TITLE, selectedTitle.toString(), getActivity());
        }
    }

    @Override
    public void onRaceStateChanged(RaceState state) {
        dataChanged(state);
        filterChanged();
    }

    @Override
    public void onJuryFlagClicked(BoatClassSeriesDataFleet group) {
        if (racesByGroup.containsKey(group)) {
            List<ManagedRace> races = racesByGroup.get(group);
            hostActivity.onProtestTimeRequested(races);
        }
    }

    /*
     * 
     * 
     * public void onJuryFlagClicked(final Group group) {
     * 
     * TimePickerDialog dialog = createJuryDialog(group); dialog.setIcon(R.drawable.ic_dialog_alert_holo_light);
     * dialog.setTitle(getString(R.string.protest_dialog_title)); dialog.show(); }
     * 
     * private TimePickerDialog createJuryDialog(final Group group) {
     * 
     * // TODO: remove this hack!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! boolean hasFinishTime =
     * false; Date latestFinish = new Date(0); for (Race race : group.getRaces()) { FlagRacingEvent latestFinishEvent =
     * new FlagRacingEvent(false, Flags.NONE, new Date(0), 0); for (RacingEvent event : race.getEventLog()) { if (event
     * instanceof FlagRacingEvent) { FlagRacingEvent flagEvent = (FlagRacingEvent) event; if
     * (flagEvent.getUpperFlag().equals(Flags.BLUE) && !flagEvent.isDisplayed()) { if
     * (latestFinishEvent.getTimeStamp().before(flagEvent.getTimeStamp())) { latestFinishEvent = flagEvent; } } } } if
     * (latestFinish.before(latestFinishEvent.getTimeStamp())) { latestFinish = latestFinishEvent.getTimeStamp();
     * hasFinishTime = true; } }
     * 
     * final Calendar presetJuryTime = Calendar.getInstance();
     * 
     * if (hasFinishTime) { presetJuryTime.setTime(latestFinish); } else { Calendar now = Calendar.getInstance();
     * now.set(Calendar.SECOND, 0); now.set(Calendar.MILLISECOND, 0); presetJuryTime.setTime(now.getTime()); } // we
     * always add one minute to round the time // so jury event is after finish event
     * presetJuryTime.add(Calendar.MINUTE, 1);
     * 
     * TimePickerDialog dialog = new TimePickerDialog(getActivity(), new OnTimeSetListener() {
     * 
     * public void onTimeSet(TimePicker view, int hourOfDay, int minute) { Date protestStartDateTime =
     * getStartOfProtestTime(hourOfDay, minute); displayJuryFlag(group, new Date(), protestStartDateTime); }
     * 
     * }, presetJuryTime.get(Calendar.HOUR_OF_DAY), presetJuryTime.get(Calendar.MINUTE), true); return dialog; }
     * 
     * private Date getStartOfProtestTime(int hourOfDay, int minute) { Calendar now = Calendar.getInstance();
     * now.set(Calendar.SECOND, 0); now.set(Calendar.MILLISECOND, 0);
     * 
     * Date pickedDate = (Date)now.getTime().clone(); pickedDate.setHours(hourOfDay); pickedDate.setMinutes(minute);
     * 
     * return pickedDate; }
     * 
     * private void displayJuryFlag(Group group, Date eventTime, Date protestTime) { for (ManagedRace race :
     * managedRacesMap.values()) { if (!race.getGroup().equals(group)) { continue; } if
     * (race.getStatus().equals(SimpleRaceStatus.FINISHED)) { race.displayJuryFlag(eventTime, protestTime); } }
     * Toast.makeText(getActivity(), String.format(getString(R.string.protest_flag_set), group.getName()),
     * Toast.LENGTH_LONG).show(); }
     */

}
