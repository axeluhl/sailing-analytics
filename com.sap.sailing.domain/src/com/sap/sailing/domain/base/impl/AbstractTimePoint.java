package com.sap.sailing.domain.base.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sap.sailing.domain.base.TimePoint;

public abstract class AbstractTimePoint implements TimePoint {
    private Date date;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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
    
    @Override
    public int hashCode() {
        return (int) (asMillis() & Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((TimePoint) obj) == 0;
    }

    @Override
    public String toString() {
        return dateFormatter.format(asDate());
    }
}
