package com.sap.sailing.racecommittee.app.ui.fragments.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.BoatClassSeriesDataFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListAdapter.JuryFlagClickedListener;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataType;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeElement;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeTitle;
import com.sap.sailing.racecommittee.app.ui.comparators.NamedRaceComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.SeriesComparator;

public class ManagedRaceListFragment extends ListFragment implements JuryFlagClickedListener, RaceStateChangedListener {

    private Serializable selectedRaceId;
    private HashMap<Serializable, ManagedRace> managedRacesById;
    private RaceListAdapter adapter;
    private ArrayList<RaceListDataType> raceDataTypeList;

    public ManagedRaceListFragment() {
        this.selectedRaceId = null;
        this.managedRacesById = new HashMap<Serializable, ManagedRace>();
        this.raceDataTypeList = new ArrayList<RaceListDataType>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // pass the localized string to the data elements...
        RaceListDataTypeElement.initializeTemplates(this);

        adapter = new RaceListAdapter(getActivity(), R.layout.welter_two_row_no_image, raceDataTypeList, this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListAdapter(adapter);
        adapter.getFilter().filter("");
    }

    @Override
    public void onStart() {
        super.onStart();
        unregisterAllRaces();
        registerAllRaces();
    }

    @Override
    public void onStop() {
        unregisterAllRaces();
        super.onStop();
    }

    private void registerAllRaces() {
        for (ManagedRace managedRace : managedRacesById.values()) {
            managedRace.getState().registerListener(this);
        }
    }

    private void unregisterAllRaces() {
        for (ManagedRace managedRace : managedRacesById.values()) {
            managedRace.getState().unregisterListener(this);
        }
    }

    public void setupOn(Collection<ManagedRace> listOfManagedRaces) {
        unregisterAllRaces();
        managedRacesById.clear();
        for (ManagedRace managedRace : listOfManagedRaces) {
            managedRacesById.put(managedRace.getId(), managedRace);
        }
        registerAllRaces();

        // initialize view
        initListElements(false);

        if (adapter != null) {
            adapter.getFilter().filter("");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRaceStateChanged(RaceState state) {
        notifyDataChanged();
    }

    private BoatClass getBoatClassForRace(ManagedRace managedRace) {
        if (managedRace.getRaceGroup().getBoatClass() == null) {
            return new BoatClassImpl(managedRace.getRaceGroup().getName(), false);
        }
        return managedRace.getRaceGroup().getBoatClass();
    }

    private void initListElements(boolean clearFirst) {

        HashMap<Serializable, RaceLogRaceStatus> savedStates = new HashMap<Serializable, RaceLogRaceStatus>();
        if (clearFirst) {
            // we have to save the previous states of the elements
            for (RaceListDataType r : raceDataTypeList) {
                if (r instanceof RaceListDataTypeElement) {
                    RaceListDataTypeElement el = (RaceListDataTypeElement) r;
                    savedStates.put(el.getRace().getId(), el.getPreviousRaceStatus());
                }
            }
            raceDataTypeList.clear();
        }

        TreeMap<BoatClassSeriesDataFleet, List<ManagedRace>> raceListHashMap = new TreeMap<BoatClassSeriesDataFleet, List<ManagedRace>>(
                new SeriesComparator());
        if (managedRacesById != null) {

            // Group Managed Races by boat class and group
            for (ManagedRace m : managedRacesById.values()) {
                BoatClassSeriesDataFleet ser = new BoatClassSeriesDataFleet(getBoatClassForRace(m), m.getSeries(),
                        m.getFleet());

                if (raceListHashMap.containsKey(ser)) {
                    raceListHashMap.get(ser).add(m);
                } else {
                    List<ManagedRace> lmr = new LinkedList<ManagedRace>();
                    lmr.add(m);
                    raceListHashMap.put(ser, lmr);
                }

            }

            for (BoatClassSeriesDataFleet key : raceListHashMap.navigableKeySet()) {
                List<ManagedRace> mrl = raceListHashMap.get(key);
                Collections.sort(mrl, new NamedRaceComparator());

                raceDataTypeList.add(new RaceListDataTypeTitle(key));

                for (ManagedRace mr : mrl) {
                    RaceListDataTypeElement dataElement = new RaceListDataTypeElement(mr);
                    if (savedStates.containsKey(mr.getId())) {
                        dataElement.setPreviousRaceStatus(savedStates.get(mr.getId()));
                    }
                    raceDataTypeList.add(dataElement);
                }
            }
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        RaceListDataType selectedItem = adapter.getItem(position);
        if (selectedItem instanceof RaceListDataTypeElement) {
            RaceListDataTypeElement selectedElement = (RaceListDataTypeElement) selectedItem;
            selectedElement.setUpdateIndicator(false);
            ((ImageView) view.findViewById(R.id.Welter_Cell_UpdateLabel)).setVisibility(View.GONE);

            ManagedRace selectedRace = selectedElement.getRace();
            this.selectedRaceId = selectedRace.getId();

            ExLog.i(ExLog.RACE_SELECTED_ELEMENT, selectedRace.getId() + " " + selectedRace.getStatus(), getActivity());

            ((RacingActivity) getActivity()).onRaceItemClicked(selectedRace);

        } else if (selectedItem instanceof RaceListDataTypeTitle) {
            RaceListDataTypeTitle selectedTitle = (RaceListDataTypeTitle) selectedItem;
            ExLog.i(ExLog.RACE_SELECTED_TITLE, selectedTitle.toString(), getActivity());
        }
    }

    public void notifyDataChanged() {
        List<RaceListDataType> list = adapter.getItems();
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) instanceof RaceListDataTypeElement) {
                RaceListDataTypeElement listElement = (RaceListDataTypeElement) list.get(i);
                ManagedRace mr = this.managedRacesById.get(listElement.getRace().getId());
                if (mr != null) {
                    if (!listElement.getPreviousRaceStatus().name().equals(mr.getStatus().name())) {
                        listElement.setRace(mr);

                        if (!listElement.getRace().getId().equals(this.selectedRaceId)) {
                            listElement.setUpdateIndicator(true);
                        }
                        // / TODO: StaticVibrator.vibrate(1000);
                    }

                }
            }
        }
        if (adapter != null) {
            adapter.getFilter().filter("");
            adapter.notifyDataSetChanged();
        }
    }

    public void onJuryFlagClicked(BoatClassSeriesDataFleet clicked) {
        // / TODO: implement.
    }

    @Override
    public void onStartTimeChanged(TimePoint startTime) {
        notifyDataChanged();
    }

    @Override
    public void onRaceAborted() {
        notifyDataChanged();
    }

    @Override
    public void onIndividualRecallDisplayed(TimePoint individualRecallRemovalFireTimePoint) {
        notifyDataChanged();
    }

    @Override
    public void onIndividualRecallRemoval() {
        notifyDataChanged();
    }

    /*
     * 
     * public void notifyDataChanged() { List<RaceListDataType> list = adapter.getItems(); for (int i = 0; i <
     * list.size(); ++i) { if (list.get(i) instanceof RaceListDataTypeElement) { RaceListDataTypeElement listElement =
     * (RaceListDataTypeElement) list.get(i); ManagedRace mr = managedRacesMap.get(listElement.getRace().getId()); if
     * (mr != null) { if (!listElement.getPreviousRaceStatus().name().equals(mr.getStatus().name())) {
     * listElement.setRace(mr);
     * 
     * if (!listElement.getRace().getId().equals(selectedRaceId)) { listElement.setUpdateIndicator(true); }
     * StaticVibrator.vibrate(1000); }
     * 
     * } } } if (adapter != null) { adapter.getFilter().filter(""); adapter.notifyDataSetChanged(); } }
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
