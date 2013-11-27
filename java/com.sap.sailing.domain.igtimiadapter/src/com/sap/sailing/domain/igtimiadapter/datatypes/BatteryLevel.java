package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public class BatteryLevel extends Fix {
    private final double percentage;
    
    public BatteryLevel(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        percentage = ((Number) valuesPerSubindex.get(1)).doubleValue();
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    protected String localToString() {
        return "Battery: "+getPercentage()+"%";
    }
}
