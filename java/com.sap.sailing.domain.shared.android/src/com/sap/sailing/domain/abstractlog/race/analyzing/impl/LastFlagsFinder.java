package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Analysis returns the most recent {@link RaceLogFlagEvent}s.
 * <p>
 * 
 * If there is no {@link RaceLogFlagEvent} in the current pass, <code>null</code> is returned. Otherwise a {@link List}
 * of {@link RaceLogFlagEvent} is returned containing all {@link RaceLogEvent}s with the most recent (logical)
 * {@link TimePoint}.
 * 
 */
public class LastFlagsFinder extends RaceLogAnalyzer<List<RaceLogFlagEvent>> {

    public LastFlagsFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected List<RaceLogFlagEvent> performAnalysis() {

        Iterator<RaceLogEvent> iterator = getPassEventsDescending().iterator();
        RaceLogFlagEvent flagEvent = getNextFlagEvent(iterator);
        if (flagEvent != null) {
            return collectAllWithSameTimePoint(iterator, flagEvent);
        }
        return null;
    }

    private List<RaceLogFlagEvent> collectAllWithSameTimePoint(Iterator<RaceLogEvent> iterator,
            RaceLogFlagEvent flagEvent) {
        List<RaceLogFlagEvent> result = new ArrayList<RaceLogFlagEvent>();
        TimePoint logicalTime = flagEvent.getLogicalTimePoint();

        while (Util.compareToWithNull(flagEvent.getLogicalTimePoint(), logicalTime, /* nullIsLess */ false) == 0) {
            result.add(flagEvent);
            flagEvent = getNextFlagEvent(iterator);
            if (flagEvent == null) {
                break;
            }
        }

        return result;
    }

    private RaceLogFlagEvent getNextFlagEvent(Iterator<RaceLogEvent> iterator) {
        while (iterator.hasNext()) {
            RaceLogEvent event = iterator.next();
            if (event instanceof RaceLogFlagEvent) {
                return (RaceLogFlagEvent) event;
            }
        }
        return null;
    }

    /**
     * Use this method to obtain the most "interesting" {@link RaceLogFlagEvent} of the {@link LastFlagsFinder}'s
     * analysis result.
     * 
     * If an empty list or <code>null</code> is passed, this method returns <code>null</code>. Otherwise the (most
     * recent by logical timestamp) {@link RaceLogFlagEvent} with the highest ID is returned favoring events with
     * {@link RaceLogFlagEvent#isDisplayed()} returning <code>true</code>.
     * 
     * @param events
     *            result of {@link LastFlagsFinder#analyze()}.
     * @return {@link RaceLogFlagEvent} or <code>null</code>.
     */
    public static RaceLogFlagEvent getMostRecent(List<RaceLogFlagEvent> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }

        List<RaceLogFlagEvent> sortedEvents = new ArrayList<RaceLogFlagEvent>(events);
        Collections.sort(sortedEvents, new Comparator<RaceLogFlagEvent>() {
            @Override
            public int compare(RaceLogFlagEvent left, RaceLogFlagEvent right) {
                int result = Util.compareToWithNull(right.getLogicalTimePoint(), left.getLogicalTimePoint(), /* nullIsLess */ false);
                if (result == 0) {
                    result = Boolean.compare(right.isDisplayed(), left.isDisplayed());
                    if (result == 0) {
                        result = right.getId().toString().compareTo(left.getId().toString());
                    }
                }
                return result;
            }
        });

        return sortedEvents.get(0);
    }

}
