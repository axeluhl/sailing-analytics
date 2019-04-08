package com.sap.sailing.domain.abstractlog.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;

/**
 * Finds all events in the log which is an {@code instanceof} {@link #ofType}.
 * 
 * @author Fredrik Teschke
 */
public class AllEventsOfTypeFinder<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>,
VisitorT> extends BaseLogAnalyzer<LogT, EventT, VisitorT, List<EventT>> {
    private final Class<?> ofType;
    private final boolean onlyUnrevoked;

    public AllEventsOfTypeFinder(LogT log, boolean onlyUnrevoked, Class<?> ofType) {
        super(log);
        this.ofType = ofType;
        this.onlyUnrevoked = onlyUnrevoked;
    }

    @Override
    protected List<EventT> performAnalysis() {
        List<EventT> result = new ArrayList<EventT>();
        Iterable<EventT> set = onlyUnrevoked ? getLog().getUnrevokedEventsDescending() : getAllEventsDescending();
        for (EventT event : set) {
            if (ofType.isAssignableFrom(event.getClass())) {
                result.add(event);
            }
        }
        return result;
    }

}
