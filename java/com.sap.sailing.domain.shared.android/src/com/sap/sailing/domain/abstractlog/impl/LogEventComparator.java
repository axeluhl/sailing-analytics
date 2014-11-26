package com.sap.sailing.domain.abstractlog.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

/**
 * Comparator sorting by pass, then by {@link AbstractLogEventAuthor}, then by {@link RaceLogEvent#getCreatedAt()}
 * timestamp.
 * 
 * If one of the passed objects is not a {@link RaceLogEvent}, sorting is done by {@link Timed#getTimePoint()}.
 */
public class LogEventComparator implements Comparator<Timed>, Serializable {
    private static final long serialVersionUID = -1337219742246147546L;
    private Comparator<Timed> timedComparator;

    public LogEventComparator() {
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
    
    protected int compareEvents(RaceLogEvent e1, RaceLogEvent e2) {
        //compare author priorities
        int result = e1.getAuthor().compareTo(e2.getAuthor());
        if (result != 0) return result;
        
        //compare created at timepoints
        result = e1.getCreatedAt().compareTo(e2.getCreatedAt());
        if (result != 0) return result;
        
        //compare logical timepoints
        result = e1.getLogicalTimePoint().compareTo(e2.getLogicalTimePoint());
        if (result != 0) return result;
        
        //compare ids
        return e1.getId().toString().compareTo(e2.getId().toString());
    }
}
