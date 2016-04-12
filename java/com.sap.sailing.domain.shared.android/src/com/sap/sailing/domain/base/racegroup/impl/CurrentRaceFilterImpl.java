package com.sap.sailing.domain.base.racegroup.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.IsFleetFragment;
import com.sap.sailing.domain.base.racegroup.IsRaceFragment;
import com.sap.sailing.domain.base.racegroup.RaceGroupFragment;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class CurrentRaceFilterImpl implements CurrentRaceFilter {

    @Override
    public <T extends RaceGroupFragment> List<T> filterCurrentRaces(Collection<T> allItems) {
        Map<IsFleetFragment, List<IsRaceFragment>> filteredRace = filterRaces(allItems);
        filteredRace = removeAllListsBevorLastStarted(filteredRace);
        return convertBackToList(filteredRace);
    }

    private <T extends RaceGroupFragment> List<T> convertBackToList(Map<IsFleetFragment, List<IsRaceFragment>> filteredRaces) {
        Map<IsFleetFragment, List<IsRaceFragment>> filteredRacesWithoutEmtyList = removeEmtyLists(filteredRaces);
        List<T> result = new LinkedList<>();
        for (IsFleetFragment currentKey : filteredRacesWithoutEmtyList.keySet()) {
            @SuppressWarnings("unchecked")
            T t = (T) currentKey; // assumes that the IsFleetFragment specialization used here also specializes T which is the special RaceGroupFragment used here
            result.add(t);
            for (IsRaceFragment r : filteredRacesWithoutEmtyList.get(currentKey)) {
                @SuppressWarnings("unchecked")
                T tRace = (T) r;// assumes that the IsRaceFragment specialization used here also specializes T which is the special RaceGroupFragment used here
                result.add(tRace);
            }
        }
        return result;
    }

    private Map<IsFleetFragment, List<IsRaceFragment>> removeEmtyLists(Map<IsFleetFragment, List<IsRaceFragment>> filteredRaces) {
        Map<IsFleetFragment, List<IsRaceFragment>> result = new LinkedHashMap<>();
        for (IsFleetFragment currentKey : filteredRaces.keySet()) {
            if (!filteredRaces.get(currentKey).isEmpty()) {
                result.put(currentKey, filteredRaces.get(currentKey));
            }
        }
        return result;
    }

    private Map<IsFleetFragment, List<IsRaceFragment>> removeAllListsBevorLastStarted(Map<IsFleetFragment, List<IsRaceFragment>> filteredRaces) {
        Map<IsFleetFragment, List<IsRaceFragment>> result;
        IsFleetFragment keyOfLastStartedRace = null;
        for (IsFleetFragment currentKey : filteredRaces.keySet()) {
            if (containsActiceRace(filteredRaces.get(currentKey))) {
                keyOfLastStartedRace = currentKey;
            }
        }
        if (keyOfLastStartedRace != null) {
            result = new LinkedHashMap<>();
            boolean found = false;
            for (IsFleetFragment currentKey : filteredRaces.keySet()) {
                if (found) {
                    result.put(currentKey, filteredRaces.get(currentKey));
                } else if (currentKey.equals(keyOfLastStartedRace)) {
                    found = true;
                    result.put(currentKey, filteredRaces.get(currentKey));
                }
            }
        } else {
            result = filteredRaces;
        }
        return result;
    }

    private boolean containsActiceRace(List<IsRaceFragment> races) {
        for (IsRaceFragment race : races) {
            if (RaceLogRaceStatus.isRunningOrFinished(race.getCurrentStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * method maps the races to fleets/series and removes all that should be not shown with "A follows B" relationship
     * 
     * @param allItems
     *            All SORTED Items
     * @return all current Races mapped to there fleet/series
     */
    private <T extends RaceGroupFragment> Map<IsFleetFragment, List<IsRaceFragment>> filterRaces(Collection<T> allItems) {
        Map<IsFleetFragment, List<IsRaceFragment>> mappedItems = mapItems(allItems);
        List<IsRaceFragment> newValueForKey = null;
        SeriesBase firstSeries = mappedItems.keySet().iterator().next().getSeries();
        for (IsFleetFragment currentKey : mappedItems.keySet()) {
            newValueForKey = filterWithFollowing(mappedItems.get(currentKey),
                    currentKey.getSeries().equals(firstSeries));
            mappedItems.put(currentKey, newValueForKey);
        }

        return mappedItems;
    }

    /**
     * Filters a List (with races of the same series and fleet).
     * 
     * @param list
     * @return
     */
    private List<IsRaceFragment> filterWithFollowing(List<IsRaceFragment> races, boolean isFirstSeries) {
        List<IsRaceFragment> result = new LinkedList<>();
        boolean addNext = isFirstSeries;
        for (IsRaceFragment currentRace : races) {
            if (hasShowingStatus(currentRace)) {
                result.add(currentRace);
                addNext = true;
            } else if (addNext) {
                result.add(currentRace);
                addNext = false;
            }
        }
        return result;
    }

    /**
     * Checks if the status is A Status to Show
     * 
     * @param currentRace
     * @return
     */
    private boolean hasShowingStatus(IsRaceFragment currentRace) {
        return RaceLogRaceStatus.isActive(currentRace.getCurrentStatus())
                || RaceLogRaceStatus.isActive(currentRace.getCurrentStatus());
    }

    /**
     * maps the Races to there Fleet/series
     * 
     * @param allItems
     * @return
     */
    private <T extends RaceGroupFragment> Map<IsFleetFragment, List<IsRaceFragment>> mapItems(Collection<T> allItems) {
        Map<IsFleetFragment, List<IsRaceFragment>> resultMap = new LinkedHashMap<>();
        IsFleetFragment currentKey = null;
        List<IsRaceFragment> currentValue = null;
        for (T currentItem : allItems) {
            if (currentItem instanceof IsFleetFragment) {
                if (currentKey != null && currentValue != null) {
                    resultMap.put(currentKey, currentValue);
                }
                currentKey = (IsFleetFragment) currentItem;
                currentValue = new LinkedList<>();
            } else {
                currentValue.add((IsRaceFragment) currentItem);
            }
        }
        if (currentKey != null && currentValue != null) {
            resultMap.put(currentKey, currentValue);
        }
        return resultMap;
    }
}
