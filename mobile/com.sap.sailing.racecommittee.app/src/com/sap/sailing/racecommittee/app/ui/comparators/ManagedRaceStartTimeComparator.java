package com.sap.sailing.racecommittee.app.ui.comparators;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import java.util.Comparator;

public class ManagedRaceStartTimeComparator implements Comparator<ManagedRace> {

    @Override
    public int compare(ManagedRace left, ManagedRace right) {
        RaceState leftState = left.getState();
        RaceState rightState = right.getState();
        if (leftState != null && rightState != null) {
            if (leftState.getStartTime() != null && rightState.getStartTime() != null) {
                return (leftState.getStartTime().asMillis() <= rightState.getStartTime().asMillis() ? -1 : 1);
            }
            if (leftState.getStartTime() != null) {
                return -1;
            }
            if (rightState.getStartTime() != null) {
                return 1;
            }
        }
        return 0;
    }
}
