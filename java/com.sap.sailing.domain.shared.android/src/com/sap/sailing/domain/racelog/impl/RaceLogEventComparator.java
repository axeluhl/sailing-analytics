package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.impl.TimedComparator;

public enum RaceLogEventComparator implements Comparator<Timed>, Serializable {
    INSTANCE;
    
    private Comparator<Timed> timedComparator;
    
    private RaceLogEventComparator() {
        this.timedComparator = TimedComparator.INSTANCE;
    }
    
    @Override
    public int compare(Timed o1, Timed o2) {
        int result = timedComparator.compare(o1, o2);
        
        
        
        return result;
    }

}
