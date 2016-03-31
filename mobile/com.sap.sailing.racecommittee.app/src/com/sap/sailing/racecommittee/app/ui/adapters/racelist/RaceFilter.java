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
        RaceListDataTypeRace currentFinishedItem = null;
        String currentUnscheduledRaceName = "";
        RaceGroupSeriesFleet currentlyRunningFleet = null;
        Map<String, Boolean> finalsStarted = new HashMap<>();
        int finishedItems = 0;
        int subItems = 0;
        List<RaceListDataTypeHeader> headersToRemove = new ArrayList<>();
        RaceListDataTypeHeader currentHeader = null;

        List<RaceListDataType> allItems = new ArrayList<>(items);
        detectStartedFinalsOrMedals(finalsStarted, allItems);

        filterByFinalOrMedal(finalsStarted, headersToRemove, allItems);

        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeHeader) {
                if (currentHeader != null && subItems > 0 && subItems == finishedItems) {
                    headersToRemove.add(currentHeader);
                }
                RaceListDataTypeHeader headerItem = (RaceListDataTypeHeader) item;
                filteredItems.add(headerItem);

                // new run for all types!
                currentHeader = (RaceListDataTypeHeader) item;
                currentUnscheduledItem = null;
                currentFinishedItem = null;
                currentlyRunningFleet = null;
                finishedItems = 0;
                subItems = 0;
            } else if (item instanceof RaceListDataTypeRace) {
                subItems++;
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
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
                    if (filteredItems.contains(currentFinishedItem)) {
                        filteredItems.remove(currentFinishedItem);
                    }
                    filteredItems.add(raceItem);
                    currentFinishedItem = raceItem;
                    currentUnscheduledItem = null;
                } else if (RaceLogRaceStatus.isActive(status)) {
                    filteredItems.add(raceItem);
                    currentlyRunningFleet = raceItem.getFleet();
                    // new run for all types!
                    currentUnscheduledItem = null;
                    currentFinishedItem = null;
                }
            }
        }
        if (currentHeader != null && subItems > 0 && subItems == finishedItems) {
            headersToRemove.add(currentHeader);
        }
        removeFinishedSeries(filteredItems, headersToRemove);
        return createResults(filteredItems);
    }

    private void filterByFinalOrMedal(Map<String, Boolean> finalsStarted, List<RaceListDataTypeHeader> headersToRemove,
        List<RaceListDataType> allItems) {
        RaceListDataTypeHeader currentHeader = null;
        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeHeader) {
                currentHeader = (RaceListDataTypeHeader) item;
            } else if (item instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                if (raceItem.getRace().getFleet().getOrdering() == 0 && !raceItem.getRace().getSeries().isMedal()) {
                    String regattaName = raceItem.getRace().getRaceGroup().getName();
                    if (finalsStarted.containsKey(regattaName) && currentHeader != null) {
                        headersToRemove.add(currentHeader);
                    }
                }
            }
        }
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
