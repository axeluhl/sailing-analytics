package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

/**
 * Finds fixed mark passings and suppressed mark passings in the {@link RaceLog}. Suppressions are encoded
 * in the result by a {@link Triple} with {@code null} as their third {@link TimePoint} component, whereas
 * those {@link Triple}s with a non-{@code null} {@link TimePoint} in their third component represent
 * fixed mark passings. The {@link Integer} second component encodes the zero-based waypoint index.
 */
public class MarkPassingDataFinder extends RaceLogAnalyzer<Set<Triple<Competitor, Integer, TimePoint>>> {

    public MarkPassingDataFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Triple<Competitor, Integer, TimePoint>> performAnalysis() {
        Set<Triple<Competitor, Integer, TimePoint>> result = new HashSet<Triple<Competitor, Integer, TimePoint>>();
        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogFixedMarkPassingEvent) {
                RaceLogFixedMarkPassingEvent castedEvent = (RaceLogFixedMarkPassingEvent) event;
                result.add(new Triple<Competitor, Integer, TimePoint>(castedEvent.getInvolvedCompetitors().get(0), castedEvent
                        .getZeroBasedIndexOfPassedWaypoint(), castedEvent.getTimePointOfFixedPassing()));
            } else if (event instanceof RaceLogSuppressedMarkPassingsEvent){
                RaceLogSuppressedMarkPassingsEvent castedEvent = (RaceLogSuppressedMarkPassingsEvent) event;
                result.add(new Triple<Competitor, Integer, TimePoint>(castedEvent.getInvolvedCompetitors().get(0), castedEvent.getZeroBasedIndexOfFirstSuppressedWaypoint(), null));
            }
        }
        return result;
    }
}
