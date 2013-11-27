package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * True heading; the difference to {@link HDGM} is the local declination at the point in time the measurement was made
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class HDG extends Fix {
    private final Bearing trueHeading;
    
    public HDG(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        trueHeading = new DegreeBearingImpl(((Number) valuesPerSubindex.get(1)).doubleValue());
    }

    public Bearing getTrueHeading() {
        return trueHeading;
    }

    @Override
    protected String localToString() {
        return "HDG: "+getTrueHeading();
    }
}
