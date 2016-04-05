package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Filter;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment.FilterMode;

/**
 * Filters races by status.
 * <p/>
 * For each "run" of unscheduled races only the first will be taken.
 * For each "run" of finished races only the last will be taken.
 */
public class RaceFilter extends Filter {

    public interface FilterSubscriber {
        void onResult(List<RaceListDataType> filtered);
    }

    protected final FilterSubscriber subscriber;
    protected Collection<RaceListDataType> items;
    protected FilterMode filterMode;

    public RaceFilter(Collection<RaceListDataType> items, FilterSubscriber resultSubscriber) {
        this.items = items;
        this.subscriber = resultSubscriber;
    }

    public void filterByMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        filter("");
    }

    protected static FilterResults createResults(Collection<RaceListDataType> result) {
        FilterResults results = new FilterResults();
        results.values = new ArrayList<>(result);
        results.count = result.size();
        return results;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if (filterMode == null || filterMode.equals(FilterMode.ALL)) {
            return createResults(items);
        }

        List<RaceListDataType> filteredItems = new ArrayList<>();
        RaceListDataTypeRace currentUnscheduledItem = null;
        String currentUnscheduledRaceName = "";
        String currentRegattaName = "";
        RaceGroupSeriesFleet currentlyRunningFleet = null;
        boolean showCurrentFleet = true;
        Map<String, Boolean> finalsStarted = new HashMap<>();
        int finishedItems = 0;
        int subItems = 0;

        List<RaceListDataTypeHeader> headersToRemove = new ArrayList<>();
        RaceListDataTypeHeader previousHeader = null;
        Map<String, Integer> fleetItemCount = new HashMap<>();
        Map<String, Integer> lastRunningRaceInFleet = new HashMap<>();

        List<RaceListDataType> allItems = new ArrayList<>(items);
        detectStartedFinalsOrMedals(finalsStarted, allItems);

        filterByFinalOrMedal(finalsStarted, headersToRemove, allItems);

        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeHeader) {
                if (previousHeader != null && subItems > 0 && subItems == finishedItems && !showCurrentFleet) {
                    headersToRemove.add(previousHeader);
                }
                showCurrentFleet = false;
                // Mark race header (including races) of subsequent series to be removed if previous series was not in last race
                if (fleetItemCount.isEmpty() && lastRunningRaceInFleet.isEmpty()) {
                    showCurrentFleet = true;
                } else {
                    for (String key : fleetItemCount.keySet()) {
                        if (fleetItemCount.get(key).equals(lastRunningRaceInFleet.get(key))) {
                            showCurrentFleet = true;
                            break;
                        }
                    }
                }
                RaceListDataTypeHeader headerItem = (RaceListDataTypeHeader) item;
                filteredItems.add(headerItem);
                // new run for all types!
                previousHeader = headerItem;
                if (!showCurrentFleet) {
                    if (currentRegattaName.equals("") || currentRegattaName.equals(headerItem.getRaceGroup().getName())) {
                        headersToRemove.add(headerItem);
                    }
                }
                fleetItemCount.clear();
                lastRunningRaceInFleet.clear();
                currentUnscheduledRaceName = "";
                currentRegattaName = previousHeader.getRaceGroup().getName();
                currentUnscheduledItem = null;
                currentlyRunningFleet = null;
                finishedItems = 0;
                subItems = 0;
            } else if (item instanceof RaceListDataTypeRace) {
                subItems++;
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                String fleetName = getFleetName(raceItem);
                countFleetItems(fleetItemCount, fleetName);
                boolean qualifying = raceItem.getRace().getFleet().getOrdering() == 0 || !raceItem.getRace().getSeries().isMedal();
                RaceLogRaceStatus status = raceItem.getCurrentStatus();
                // Preserve first unscheduled races that belong to the first race column of the leaderboard
                if (qualifying && RaceLogRaceStatus.UNSCHEDULED.equals(status)) {
                    if (currentUnscheduledRaceName.equals("")) {
                        currentUnscheduledRaceName = raceItem.getRaceName();
                    }
                    if (currentUnscheduledRaceName.equals(raceItem.getRaceName())) {
                        filteredItems.add(raceItem);
                        if (currentlyRunningFleet == null) {
                            currentUnscheduledItem = raceItem;
                        }
                    }
                }
                if (currentUnscheduledItem == null && RaceLogRaceStatus.UNSCHEDULED.equals(status)) {
                    if (currentlyRunningFleet == null || currentlyRunningFleet.equals(raceItem.getFleet())) {
                        currentUnscheduledItem = raceItem;
                        if (!filteredItems.contains(raceItem)) {
                            filteredItems.add(raceItem);
                        }
                    }
                } else if (RaceLogRaceStatus.FINISHED.equals(status)) {
                    finishedItems++;
                    if (filteredItems.contains(raceItem)) {
                        filteredItems.remove(raceItem);
                    }
                    int fleetCount = fleetItemCount.get(getFleetName(raceItem));
                    lastRunningRaceInFleet.put(getFleetName(raceItem), fleetCount);
                    currentlyRunningFleet = raceItem.getFleet();
                    currentUnscheduledItem = null;
                } else if (RaceLogRaceStatus.isActive(status)) {
                    int fleetCount = fleetItemCount.get(getFleetName(raceItem));
                    lastRunningRaceInFleet.put(getFleetName(raceItem), fleetCount);
                    filteredItems.add(raceItem);
                    currentUnscheduledItem = null;
                    currentlyRunningFleet = raceItem.getFleet();
                }
            }
        } if (previousHeader != null && subItems > 0 && subItems == finishedItems) {
            headersToRemove.add(previousHeader);
        }
        removeFinishedSeries(filteredItems, headersToRemove);
        return createResults(filteredItems);
    }

    private String getFleetName(RaceListDataTypeRace raceItem) {
        return raceItem.getRace().getIdentifier().getFleet().getName();
    }

    private void countFleetItems(Map<String, Integer> fleetItemCount, String fleetName) {
        if (!fleetItemCount.containsKey(fleetName)) {
            fleetItemCount.put(fleetName, 0);
        }
        int count = fleetItemCount.get(fleetName) + 1;
        fleetItemCount.put(fleetName, count);
    }

    private void filterByFinalOrMedal(Map<String, Boolean> finalsStarted, List<RaceListDataTypeHeader> headersToRemove,
        List<RaceListDataType> allItems) {
        RaceListDataTypeHeader currentHeader = null;
        List<RaceListDataType> racesToRemove = new ArrayList<>();
        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeHeader) {
                currentHeader = (RaceListDataTypeHeader) item;
            } else if (item instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                if (raceItem.getRace().getFleet().getOrdering() == 0 && !raceItem.getRace().getSeries().isMedal()) {
                    String regattaName = raceItem.getRace().getRaceGroup().getName();
                    if (finalsStarted.containsKey(regattaName) && currentHeader != null) {
                        headersToRemove.add(currentHeader);
                        racesToRemove.add(raceItem);
                    }
                }
            }
        }
        allItems.removeAll(racesToRemove);
    }

    private void detectStartedFinalsOrMedals(Map<String, Boolean> finalsStarted, List<RaceListDataType> allItems) {
        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                String regattaName = raceItem.getRace().getRaceGroup().getName();
                if (raceItem.getRace().getFleet().getOrdering() != 0 || raceItem.getRace().getSeries().isMedal()) {
                    if (RaceLogRaceStatus.isRunningOrFinished(raceItem.getCurrentStatus())) {
                        finalsStarted.put(regattaName, true);
                    }
                }
            }
        }
    }

    private void removeFinishedSeries(List<RaceListDataType> filteredItems, List<RaceListDataTypeHeader> headersToRemove) {
        List<RaceListDataType> itemsToKeep = new ArrayList<>();
        boolean keepItems = false;
        for (RaceListDataType item : filteredItems) {
            if (item instanceof RaceListDataTypeHeader) {
                keepItems = !headersToRemove.contains(item);
                if (keepItems) {
                    itemsToKeep.add(item);
                }

            } else if (item instanceof RaceListDataTypeRace) {
                if (keepItems) {
                    itemsToKeep.add(item);
                }
            }
        }
        filteredItems.clear();
        filteredItems.addAll(itemsToKeep);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        subscriber.onResult((List<RaceListDataType>) results.values);
    }

}
