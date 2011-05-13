package com.sap.sailing.domain.base.impl;

import java.util.Date;

import com.sap.sailing.domain.base.TimePoint;

public abstract class AbstractTimePoint implements TimePoint {
    @Override
    public int compareTo(TimePoint o) {
        long nanoDiff = asNanos() - o.asNanos();
        return nanoDiff<0 ?  -1 : nanoDiff == 0 ? 0 : 1;
    }

    private Date date;

    @Override
    public long asMillis() {
        return asNanos() / 1000000l;
    }

    @Override
    public long asNanos() {
        return asMillis() * 1000000l;
    }

    @Override
    public Date asDate() {
        if (date == null) {
            date = new Date(asMillis());
        }
        return date;
    }
}
