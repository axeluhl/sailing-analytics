package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public class GpsQualitySatCount extends Fix {
    private final int satCount;
    
    public GpsQualitySatCount(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        satCount = ((Number) valuesPerSubindex.get(1)).intValue();
    }

    public int getSatCount() {
        return satCount;
    }
}
