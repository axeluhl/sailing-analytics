package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Date;

public interface TimePoint extends Comparable<TimePoint>, Serializable {
    long asMillis();

    Date asDate();
}
