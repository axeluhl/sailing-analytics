package com.sap.sailing.domain.base.impl;

import java.util.Date;

import com.sap.sailing.domain.base.TimePoint;

public abstract class AbstractTimePoint implements TimePoint {
    private Date date;

    @Override
    public int compareTo(TimePoint o) {
        long milliDiff = asMillis() - o.asMillis();
        return milliDiff<0 ?  -1 : milliDiff == 0 ? 0 : 1;
    }

    @Override
    public Date asDate() {
        if (date == null) {
            date = new Date(asMillis());
        }
        return date;
    }
}
