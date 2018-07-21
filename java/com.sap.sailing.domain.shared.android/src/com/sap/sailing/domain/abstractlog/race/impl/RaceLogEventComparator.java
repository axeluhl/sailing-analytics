package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Comparator;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventComparator;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sse.common.Timed;

/**
 * Comparator sorting by pass, then by {@link AbstractLogEventAuthor}, then by {@link RaceLogEvent#getCreatedAt()}
 * timestamp.
 * 
 * If one of the passed objects is not a {@link RaceLogEvent}, sorting is done by {@link Timed#getTimePoint()}.
 */
public class RaceLogEventComparator extends LogEventComparator implements Comparator<Timed>, Serializable {
    private static final long serialVersionUID = 6146135178558781346L;

    protected int compareEvents(AbstractLogEvent<?> e1, AbstractLogEvent<?> e2) {
        //compare passes
        if (e1 instanceof RaceLogEvent && e2 instanceof RaceLogEvent) {
            int result = ((RaceLogEvent) e1).getPassId() - ((RaceLogEvent) e2).getPassId();
            if (result != 0) return result;
        }
        
        return super.compareEvents(e1, e2);
    }
}
