package com.sap.sailing.domain.base.racegroup.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.FilterableRace;
import com.sap.sailing.domain.base.racegroup.IsFleetFragment;
import com.sap.sailing.domain.base.racegroup.IsRaceFragment;
import com.sap.sailing.domain.base.racegroup.RaceGroupFragment;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class CurrentRaceFilterImpl implements CurrentRaceFilter {

    @Override
    public <T extends FilterableRace> Set<T> filterCurrentRaces(Set<T> allRaces) {
        final Set<T> filteredRaces = new HashSet<>();
        // TODO implement the CurrentRaceFilter algorithm here...
        filteredRaces.addAll(allRaces);
        return filteredRaces;
    }

    private Map<IsFleetFragment, List<IsRaceFragment>> removeAllListsBeforeLastStarted(Map<IsFleetFragment, List<IsRaceFragment>> filteredRaces) {
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
