package com.sap.sailing.domain.abstractlog.impl;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;

/**
 * Finds the most recent event in the log which is an {@code instanceof} {@link #ofType}.
 * @author Fredrik Teschke
 *
 */
public class LastEventOfTypeFinder<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>,
VisitorT> extends BaseLogAnalyzer<LogT, EventT, VisitorT, EventT> {
    private final Class<?> ofType;
    private final boolean onlyUnrevoked;

    public LastEventOfTypeFinder(LogT log, boolean onlyUnrevoked, Class<?> ofType) {
        super(log);
        this.ofType = ofType;
        this.onlyUnrevoked = onlyUnrevoked;
    }

    @Override
    protected EventT performAnalysis() {
        Iterable<EventT> set = onlyUnrevoked ? getLog().getUnrevokedEventsDescending() : getAllEventsDescending();
        for (EventT event : set) {
            if (ofType.isAssignableFrom(event.getClass())) {
                return event;
            }
        }
        return null;
    }

}
