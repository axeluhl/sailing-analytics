package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Date;

public interface TimePoint extends Comparable<TimePoint>, Serializable {
    long asMillis();

    Date asDate();
    
    TimePoint plus(long milliseconds);
    
    boolean after(TimePoint other);
    
    boolean before(TimePoint other);
}
