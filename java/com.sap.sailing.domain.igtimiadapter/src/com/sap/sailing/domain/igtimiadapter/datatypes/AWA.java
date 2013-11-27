package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * Apparent wind angle, relative to the device's or vessel's orientation
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AWA extends Fix {
    private final Bearing apparentWindAngle;
    
    public AWA(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        apparentWindAngle = new DegreeBearingImpl(((Number) valuesPerSubindex.get(1)).doubleValue());
    }

    public Bearing getApparentWindAngle() {
        return apparentWindAngle;
    }

    @Override
    protected String localToString() {
        return "AWA: "+getApparentWindAngle();
    }
}
