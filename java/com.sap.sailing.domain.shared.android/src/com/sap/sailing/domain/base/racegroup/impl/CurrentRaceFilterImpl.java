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

public class CurrentRaceFilterImpl<ITEM extends RaceGroupFragment, SERIES extends IsFleetFragment, RACE extends IsRaceFragment>
        implements CurrentRaceFilter<ITEM, SERIES, RACE> {

    @Override
    public List<ITEM> filterCurrentRaces(Collection<ITEM> allItems) {
        Map<SERIES, List<RACE>> filteredRace = filterRaces(allItems);
        filteredRace = removeAllListsBevorLastStarted(filteredRace);
        return convertBackToList(filteredRace);
    }

    @SuppressWarnings("unchecked")
    private List<ITEM> convertBackToList(Map<SERIES, List<RACE>> filteredRaces) {
        Map<SERIES, List<RACE>> filteredRacesWithoutEmtyList = removeEmtyLists(filteredRaces);
        List<ITEM> result = new LinkedList<>();
        for (SERIES currentKey : filteredRacesWithoutEmtyList.keySet()) {
            result.add((ITEM) currentKey);
            result.addAll((Collection<? extends ITEM>) filteredRacesWithoutEmtyList.get(currentKey));
        }
        return result;
    }

    private Map<SERIES, List<RACE>> removeEmtyLists(Map<SERIES, List<RACE>> filteredRaces) {
        Map<SERIES, List<RACE>> result = new LinkedHashMap<>();
        for (SERIES currentKey : filteredRaces.keySet()) {
            if (!filteredRaces.get(currentKey).isEmpty()) {
                result.put(currentKey, filteredRaces.get(currentKey));
            }
        }
        return result;
    }

    private Map<SERIES, List<RACE>> removeAllListsBevorLastStarted(Map<SERIES, List<RACE>> filteredRaces) {
        Map<SERIES, List<RACE>> result;
        SERIES keyOfLastStartedRace = null;
        for (SERIES currentKey : filteredRaces.keySet()) {
            if (containsActiceRace(filteredRaces.get(currentKey))) {
                keyOfLastStartedRace = currentKey;
            }
        }
        if (keyOfLastStartedRace != null) {
            result = new LinkedHashMap<>();
            boolean found = false;
            for (SERIES currentKey : filteredRaces.keySet()) {
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

    private boolean containsActiceRace(List<RACE> races) {
        for (RACE race : races) {
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
    private Map<SERIES, List<RACE>> filterRaces(Collection<ITEM> allItems) {
        Map<SERIES, List<RACE>> mappedItems = mapItems(allItems);
        List<RACE> newValueForKey = null;
        SeriesBase firstSeries = mappedItems.keySet().iterator().next().getSeries();
        for (SERIES currentKey : mappedItems.keySet()) {
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
    private List<RACE> filterWithFollowing(List<RACE> races, boolean isFirstSeries) {
        List<RACE> result = new LinkedList<>();
        boolean addNext = isFirstSeries;
        for (RACE currentRace : races) {
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
    private boolean hasShowingStatus(RACE currentRace) {
        return RaceLogRaceStatus.isActive(currentRace.getCurrentStatus())
                || RaceLogRaceStatus.isActive(currentRace.getCurrentStatus());
    }

    /**
     * maps the Races to there Fleet/series
     * 
     * @param allItems
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<SERIES, List<RACE>> mapItems(Collection<ITEM> allItems) {
        Map<SERIES, List<RACE>> resultMap = new LinkedHashMap<>();
        SERIES currentKey = null;
        List<RACE> currentValue = null;
        for (ITEM currentItem : allItems) {
            if (currentItem instanceof IsFleetFragment) {
                if (currentKey != null && currentValue != null) {
                    resultMap.put(currentKey, currentValue);
                }
                currentKey = (SERIES) currentItem;
                currentValue = new LinkedList<>();
            } else {
                currentValue.add((RACE) currentItem);
            }
        }
        if (currentKey != null && currentValue != null) {
            resultMap.put(currentKey, currentValue);
        }

        return resultMap;
    }
}
