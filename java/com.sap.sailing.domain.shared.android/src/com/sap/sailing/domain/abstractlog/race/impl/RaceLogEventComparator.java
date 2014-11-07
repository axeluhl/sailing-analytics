package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

/**
 * Comparator sorting by pass, then by {@link RaceLogEventAuthor}, then by {@link RaceLogEvent#getCreatedAt()}
 * timestamp.
 * 
 * If one of the passed objects is not a {@link RaceLogEvent}, sorting is done by {@link Timed#getTimePoint()}.
 */
public enum RaceLogEventComparator implements Comparator<Timed>, Serializable {
    INSTANCE;

    private Comparator<Timed> timedComparator;

    private RaceLogEventComparator() {
        this.timedComparator = TimedComparator.INSTANCE;
    }

    @Override
    public int compare(Timed o1, Timed o2) {
        if (o1 instanceof RaceLogEvent && o2 instanceof RaceLogEvent) {
            return compareEvents((RaceLogEvent) o1, (RaceLogEvent) o2);
        }

        // fallback to timed comparison
        return timedComparator.compare(o1, o2);
    }

    private int compareEvents(RaceLogEvent e1, RaceLogEvent e2) {
        int result = comparePasses(e1, e2);
        return result == 0 ? compareAuthorPriorities(e1, e2) : result;
    }

    private int comparePasses(RaceLogEvent e1, RaceLogEvent e2) {
        return e1.getPassId() - e2.getPassId();
    }

    private int compareAuthorPriorities(RaceLogEvent e1, RaceLogEvent e2) {
        int result = e1.getAuthor().compareTo(e2.getAuthor());
        return result == 0 ? compareCreatedAtTimes(e1, e2) : result;
    }

    private int compareCreatedAtTimes(RaceLogEvent e1, RaceLogEvent e2) {
        int result = e1.getCreatedAt().compareTo(e2.getCreatedAt());
        return result == 0 ? compareIds(e1, e2) : result;
    }

    private int compareIds(RaceLogEvent e1, RaceLogEvent e2) {
        return e1.getId().toString().compareTo(e2.getId().toString());
    }

}
