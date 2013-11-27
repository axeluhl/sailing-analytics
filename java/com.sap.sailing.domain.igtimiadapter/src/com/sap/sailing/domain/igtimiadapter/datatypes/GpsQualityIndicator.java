package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public class GpsQualityIndicator extends Fix {
    private final int quality;
    
    public GpsQualityIndicator(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        quality = ((Number) valuesPerSubindex.get(1)).intValue();
    }

    public int getQuality() {
        return quality;
    }

    @Override
    protected String localToString() {
        return "GPS Quality: "+getQuality();
    }
}
