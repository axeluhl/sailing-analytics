package com.sap.sailing.windestimation.impl;

import java.util.Iterator;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class WindDirectionCandidatesForTimePointIterationHelper {

    private final Iterable<WindDirectionCandidatesForManeuver> windDirectionCandidatesForTimePoints;
    
    private Iterator<WindDirectionCandidatesForManeuver> iterator;
    private WindDirectionCandidatesForManeuver previous = null;
    private WindDirectionCandidatesForManeuver next = null;

    public WindDirectionCandidatesForTimePointIterationHelper(Iterable<WindDirectionCandidatesForManeuver> windDirectionCandidatesForTimePoints) {
        this.windDirectionCandidatesForTimePoints = windDirectionCandidatesForTimePoints;
        resetIterationState();
    }
    
    public WindDirectionCandidatesForManeuver getWindDirectionCandidatesWithTimePointClosestTo(TimePoint timePoint) {
        while(next != null && !next.getTimePoint().after(timePoint)) {
            previous = next;
            next = iterator.hasNext() ? iterator.next() : null;
        }
        if(next != null) {
            Duration durationUntilNext = timePoint.until(next.getTimePoint());
            Duration durationFromPrevious = previous.getTimePoint().until(timePoint);
            if(durationUntilNext.compareTo(durationFromPrevious) < 0) {
                return next;
            }
        }
        return previous;
    }
    
    public void resetIterationState() {
        iterator = windDirectionCandidatesForTimePoints.iterator();
        previous = iterator.hasNext() ? iterator.next() : null;
        next = iterator.hasNext() ? iterator.next() : null;
    }
    
    public Iterable<WindDirectionCandidatesForManeuver> getWindDirectionCandidatesForTimePoints() {
        return windDirectionCandidatesForTimePoints;
    }

}
