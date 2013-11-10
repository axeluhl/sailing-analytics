package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public enum RaceLogEventComparator implements Comparator<Timed>, Serializable {
    INSTANCE;
    
    @Override
    public int compare(Timed o1, Timed o2) {
        if (o1 instanceof RaceLogEvent && o2 instanceof RaceLogEvent) {
            return compareEvents((RaceLogEvent)o1, (RaceLogEvent)o2);
        }
        
        throw new UnsupportedOperationException("Must provide objects of type " + RaceLogEvent.class.getName());
    }

    private int compareEvents(RaceLogEvent e1, RaceLogEvent e2) {
        int result = comparePasses(e1, e2);
        return result == 0 ? comparePriorities(e1, e2) : result;
    }

    private int comparePasses(RaceLogEvent e1, RaceLogEvent e2) {
        return e1.getPassId() - e2.getPassId();
    }
    
    private int comparePriorities(RaceLogEvent e1, RaceLogEvent e2) {
        int result =  new Integer(e2.getAuthor().getPriority()).compareTo(e1.getAuthor().getPriority());
        return result == 0 ? compareCreatedAtTimes(e1, e2) : result;
    }

    private int compareCreatedAtTimes(RaceLogEvent e1, RaceLogEvent e2) {
        int result =  e1.getCreatedAt().compareTo(e2.getCreatedAt());
        return result == 0 ? compareIds(e1, e2) : result;
    }

    private int compareIds(RaceLogEvent e1, RaceLogEvent e2) {
        return e1.getId().toString().compareTo(e2.getId().toString());
    }

}
