package com.sap.sse.common;

import java.io.Serializable;
import java.util.Date;

public interface TimePoint extends Comparable<TimePoint>, Serializable {
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
