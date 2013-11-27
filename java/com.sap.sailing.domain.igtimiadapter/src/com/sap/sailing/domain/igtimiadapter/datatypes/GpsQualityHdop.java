package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public class GpsQualityHdop extends Fix {
    private final Distance hdop;
    
    public GpsQualityHdop(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        hdop = new MeterDistance(((Number) valuesPerSubindex.get(1)).doubleValue());
    }

    public Distance getHdop() {
        return hdop;
    }

    @Override
    protected String localToString() {
        return "HDOP: "+getHdop();
    }
}
