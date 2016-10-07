package com.sap.sse.common;

import java.io.Serializable;
import java.util.Date;

import com.sap.sse.common.impl.MillisecondsTimePoint;

public interface TimePoint extends Comparable<TimePoint>, Serializable {
    // JavaScript / ECMAScript Date can only handle a value range of -100,000,000 days before
    // and after the beginning of the Epoch. If we want these constants to serialize losslessly
    // to JavaScript / GWT, we need to accept this constraint.
    TimePoint BeginningOfTime = new MillisecondsTimePoint(-100000000l * 24 * 3600);
    TimePoint EndOfTime = new MillisecondsTimePoint(100000000l * 24 * 3600);
    
    long asMillis();

    Date asDate();
    
    TimePoint plus(long milliseconds);
    
    TimePoint plus(Duration duration);
    
    TimePoint minus(long milliseconds);
    
    TimePoint minus(Duration duration);
    
    /**
     * The duration that {@code later} is after {@code this}. May be negative if {@code later} is before {@code this}.
     */
    Duration until(TimePoint later);
    
    boolean after(TimePoint other);
    
    boolean before(TimePoint other);

    /**
     * @return a {@link TimePoint} such that when used to set the time of a calendar, the seconds and milliseconds will
     *         match that of {@code syncToClosestSecondsAndMillisOf} and the time difference to {@code this} time point is
     *         minimal. In particular, the time difference will always be less than or equal to 30s.
     */
    TimePoint getNearestModuloOneMinute(TimePoint syncToClosestSecondsAndMillisOf);
}
