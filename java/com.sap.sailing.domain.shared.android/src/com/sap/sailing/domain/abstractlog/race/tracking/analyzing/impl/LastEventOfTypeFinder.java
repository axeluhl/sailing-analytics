package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;

/**
 * Finds the most recent event in the racelog which is an {@code instanceof} {@link #ofType}.
 * @author Fredrik Teschke
 *
 */
public class LastEventOfTypeFinder extends RaceLogAnalyzer<RaceLogEvent> {
    private final Class<?> ofType;
    private final boolean onlyUnrevoked;

    public LastEventOfTypeFinder(RaceLog raceLog, boolean onlyUnrevoked, Class<?> ofType) {
        super(raceLog);
        this.ofType = ofType;
        this.onlyUnrevoked = onlyUnrevoked;
    }

    @Override
    protected RaceLogEvent performAnalysis() {
        Iterable<RaceLogEvent> set = onlyUnrevoked ? raceLog.getUnrevokedEventsDescending() : getAllEventsDescending();
        for (RaceLogEvent event : set) {
            if (ofType.isAssignableFrom(event.getClass())) {
                return event;
            }
        }
        return null;
    }

}
