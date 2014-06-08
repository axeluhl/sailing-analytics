package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.FixedMarkPassingEvent;
import com.sap.sse.common.Util.Triple;

public class FixedMarkPassingsAnalyzer extends RaceLogAnalyzer<Set<Triple<Competitor, Integer, TimePoint>>> {

    public FixedMarkPassingsAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Triple<Competitor, Integer, TimePoint>> performAnalysis() {
        Set<Triple<Competitor, Integer, TimePoint>> result = new HashSet<Triple<Competitor, Integer, TimePoint>>();

        for (RaceLogEvent event : getRaceLog().getUnrevokedEvents()) {
            if (event instanceof FixedMarkPassingEvent) {

                FixedMarkPassingEvent castedEvent = (FixedMarkPassingEvent) event;
                result.add(new Triple<Competitor, Integer, TimePoint>(castedEvent.getInvolvedBoats().get(0), castedEvent
                        .getZeroBasedIndexOfPassedWaypoint(), castedEvent.getTimePoint()));
            }
        }
        return result;
    }
}
