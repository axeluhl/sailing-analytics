package com.sap.sailing.domain.tracking.impl;

import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;

public class TimedComparator implements Comparator<Timed> {
    public static final Comparator<Timed> INSTANCE = new TimedComparator();

    @Override
    public int compare(Timed o1, Timed o2) {
        return o1.getTimePoint().compareTo(o2.getTimePoint());
    }
}

