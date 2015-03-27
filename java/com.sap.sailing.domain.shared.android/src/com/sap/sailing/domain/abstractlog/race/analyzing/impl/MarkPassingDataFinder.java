package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.FixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.SuppressedMarkPassingsEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class MarkPassingDataFinder extends RaceLogAnalyzer<Set<Triple<Competitor, Integer, TimePoint>>> {

    public MarkPassingDataFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Set<Triple<Competitor, Integer, TimePoint>> performAnalysis() {
        Set<Triple<Competitor, Integer, TimePoint>> result = new HashSet<Triple<Competitor, Integer, TimePoint>>();
        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof FixedMarkPassingEvent) {
                FixedMarkPassingEvent castedEvent = (FixedMarkPassingEvent) event;
                result.add(new Triple<Competitor, Integer, TimePoint>(castedEvent.getInvolvedBoats().get(0), castedEvent
                        .getZeroBasedIndexOfPassedWaypoint(), castedEvent.getTimePointOfFixedPassing()));
            } else if (event instanceof SuppressedMarkPassingsEvent){
                SuppressedMarkPassingsEvent castedEvent = (SuppressedMarkPassingsEvent) event;
                result.add(new Triple<Competitor, Integer, TimePoint>(castedEvent.getInvolvedBoats().get(0), castedEvent.getZeroBasedIndexOfFirstSuppressedWaypoint(), null));
            }
        }
        return result;
    }
}
