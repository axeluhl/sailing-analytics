package com.sap.sailing.domain.base;

import java.util.Date;

public interface TimePoint extends Comparable<TimePoint> {
    long asMillis();

    Date asDate();
}
