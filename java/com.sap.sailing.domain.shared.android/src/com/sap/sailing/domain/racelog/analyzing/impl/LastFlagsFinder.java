package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

/**
 * @author I074137
 *
 */
public class LastFlagsFinder extends RaceLogAnalyzer<List<RaceLogFlagEvent>> {

    public LastFlagsFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected List<RaceLogFlagEvent> performAnalyzation() {
        
        Iterator<RaceLogEvent> iterator = getPassEventsDescending().iterator();
        RaceLogFlagEvent flagEvent = getNextFlagEvent(iterator);
        if (flagEvent != null) {
            return collectAllWithSameTimePoint(iterator, flagEvent);
        }
        return new ArrayList<RaceLogFlagEvent>();
    }

    private List<RaceLogFlagEvent> collectAllWithSameTimePoint(Iterator<RaceLogEvent> iterator, RaceLogFlagEvent flagEvent) {
        List<RaceLogFlagEvent> result = new ArrayList<RaceLogFlagEvent>();
        TimePoint lastFlagsTime = flagEvent.getTimePoint();
        
        while(flagEvent.getTimePoint().equals(lastFlagsTime)) {
            result.add(flagEvent);
            flagEvent = getNextFlagEvent(iterator);
            if (flagEvent == null) {
                break;
            }
        }
        
        return result;
    }

    private RaceLogFlagEvent getNextFlagEvent(Iterator<RaceLogEvent> iterator) {
        while(iterator.hasNext()) {
            RaceLogEvent event = iterator.next();
            if (event instanceof RaceLogFlagEvent) {
                return (RaceLogFlagEvent)event;
            }
        }
        return null;
    }

}
