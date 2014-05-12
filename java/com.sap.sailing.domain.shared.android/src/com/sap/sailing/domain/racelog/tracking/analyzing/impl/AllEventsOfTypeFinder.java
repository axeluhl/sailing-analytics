package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;

/**
 * Finds all events in the racelog which is an {@code instanceof} {@link #ofType}.
 * @author Fredrik Teschke
 *
 */
public class AllEventsOfTypeFinder extends RaceLogAnalyzer<List<RaceLogEvent>> {
    private final Class<?> ofType;
    private final boolean onlyUnrevoked;

    public AllEventsOfTypeFinder(RaceLog raceLog, boolean onlyUnrevoked, Class<?> ofType) {
        super(raceLog);
        this.ofType = ofType;
        this.onlyUnrevoked = onlyUnrevoked;
    }

    @Override
    protected List<RaceLogEvent> performAnalysis() {
        List<RaceLogEvent> result = new ArrayList<RaceLogEvent>();
        Iterable<RaceLogEvent> set = onlyUnrevoked ? raceLog.getUnrevokedEventsDescending() : getAllEventsDescending();
        for (RaceLogEvent event : set) {
            if (ofType.isAssignableFrom(event.getClass())) {
                result.add(event);
            }
        }
        return result;
    }

}
