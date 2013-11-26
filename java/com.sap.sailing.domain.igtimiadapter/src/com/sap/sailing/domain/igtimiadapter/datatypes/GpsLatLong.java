package com.sap.sailing.domain.igtimiadapter.datatypes;

import com.sap.sailing.domain.common.TimePoint;

public class GpsLatLong extends Fix {
    public GpsLatLong(TimePoint timePoint, String sensorId) {
        super(/* type */ 1, sensorId, timePoint);
    }
}
