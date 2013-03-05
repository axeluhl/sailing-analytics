package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

public enum RaceLogEventComparator implements Comparator<Timed>, Serializable {
    INSTANCE;
    
    private Comparator<Timed> timedComparator;
    
    private RaceLogEventComparator() {
        this.timedComparator = TimedComparator.INSTANCE;
    }
    
    @Override
    public int compare(Timed o1, Timed o2) {
        if (o1 instanceof RaceLogEvent && o2 instanceof RaceLogEvent) {
            return compareEvents((RaceLogEvent)o1, (RaceLogEvent)o2);
        }
        
        // fallback to timed comparison
        return timedComparator.compare(o1, o2);
    }

    private int compareEvents(RaceLogEvent e1, RaceLogEvent e2) {
        int result = comparePasses(e1, e2);
        return result == 0 ? timedComparator.compare(e1, e2) : result;
    }

    private int comparePasses(RaceLogEvent e1, RaceLogEvent e2) {
        return e1.getPassId() - e2.getPassId();
    }

}
