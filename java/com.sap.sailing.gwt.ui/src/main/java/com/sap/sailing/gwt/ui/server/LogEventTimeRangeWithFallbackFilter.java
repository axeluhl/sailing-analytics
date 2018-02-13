package com.sap.sailing.gwt.ui.server;

import java.util.NavigableMap;
import java.util.TreeMap;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;

/**
 * Helper class that filters given {@link AbstractLogEvent AbstractLogEvents} based on a given {@link TimeRange}. All
 * {@link AbstractLogEvent AbstractLogEvents} in the given {@link TimeRange} plus the nearest ones outside of the
 * {@link TimeRange} are retained.
 */
public class LogEventTimeRangeWithFallbackFilter<E extends AbstractLogEvent<?>> {
    
    private final NavigableMap<TimePoint, E> filteredEvents;
    private final TimeRange timeRangeToInclude;
    
    public LogEventTimeRangeWithFallbackFilter(TimeRange timeRangeToInclude) {
        this.timeRangeToInclude = timeRangeToInclude;
        this.filteredEvents = new TreeMap<>();
    }
    
    public void addEvent(E event) {
        final TimePoint logicalTimePoint = event.getLogicalTimePoint();
        if (this.timeRangeToInclude.includes(logicalTimePoint)) {
            filteredEvents.put(logicalTimePoint, event);
        } else if (logicalTimePoint.before(this.timeRangeToInclude.from())) {
            final TimePoint currentFallbackBefore = this.filteredEvents.lowerKey(this.timeRangeToInclude.from());
            if (currentFallbackBefore == null || logicalTimePoint.after(currentFallbackBefore)) {
                if (currentFallbackBefore != null) {
                    this.filteredEvents.remove(currentFallbackBefore);
                }
                filteredEvents.put(logicalTimePoint, event);
            }
        } else if (logicalTimePoint.after(this.timeRangeToInclude.to())) {
            final TimePoint currentFallbackAfter = this.filteredEvents.higherKey(this.timeRangeToInclude.to());
            if (currentFallbackAfter == null || logicalTimePoint.before(currentFallbackAfter)) {
                if (currentFallbackAfter != null) {
                    this.filteredEvents.remove(currentFallbackAfter);
                }
                filteredEvents.put(logicalTimePoint, event);
            }
        }
    }

    public Iterable<E> getFilteredEvents() {
        return this.filteredEvents.values();
    }
}
