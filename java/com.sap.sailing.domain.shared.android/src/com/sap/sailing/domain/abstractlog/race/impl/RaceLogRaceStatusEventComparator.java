package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;

/**
 * Comparator sorting by pass, then by state order number, then by {@link RaceLogEventAuthor}, then by {@link RaceLogEvent#getCreatedAt()} timestamp.
 */
public enum RaceLogRaceStatusEventComparator implements Comparator<RaceLogRaceStatusEvent>, Serializable {
    INSTANCE;

    private RaceLogRaceStatusEventComparator() {
    }

    @Override
    public int compare(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        int result = comparePasses(e1, e2);
        return result == 0 ? compareRaceStatus(e1, e2) : result;
    }

    private int comparePasses(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        return e1.getPassId() - e2.getPassId();
    }

    private int compareRaceStatus(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        int result = e1.getNextStatus().getOrderNumber() - e2.getNextStatus().getOrderNumber(); 
            
        return result == 0 ? compareAuthorPriorities(e1, e2) : result;
    }

    private int compareAuthorPriorities(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        int result = e1.getAuthor().compareTo(e2.getAuthor());
        return result == 0 ? compareCreatedAtTimes(e1, e2) : result;
    }

    private int compareCreatedAtTimes(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        int result = e1.getCreatedAt().compareTo(e2.getCreatedAt());
        return result == 0 ? compareIds(e1, e2) : result;
    }

    private int compareIds(RaceLogRaceStatusEvent e1, RaceLogRaceStatusEvent e2) {
        return e1.getId().toString().compareTo(e2.getId().toString());
    }

}
