package com.sap.sse.common;

import java.io.Serializable;
import java.util.Date;

import com.sap.sse.common.impl.MillisecondsTimePoint;

public interface TimePoint extends Comparable<TimePoint>, Serializable {
    TimePoint BeginningOfTime = new MillisecondsTimePoint(Long.MIN_VALUE);
    TimePoint EndOfTime = new MillisecondsTimePoint(Long.MAX_VALUE);
    
    long asMillis();

    Date asDate();
    
    TimePoint plus(long milliseconds);
    
    TimePoint plus(Duration duration);
    
    TimePoint minus(long milliseconds);
    
    TimePoint minus(Duration duration);
    
    Duration until(TimePoint later);
    
    boolean after(TimePoint other);
    
    boolean before(TimePoint other);
}
