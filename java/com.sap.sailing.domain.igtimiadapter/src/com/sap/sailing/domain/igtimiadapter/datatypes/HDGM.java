package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * Magnetic heading
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class HDGM extends Fix {
    private final Bearing magnetigHeading;
    
    public HDGM(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        magnetigHeading = new DegreeBearingImpl(((Number) valuesPerSubindex.get(1)).doubleValue());
    }

    public Bearing getMagnetigHeading() {
        return magnetigHeading;
    }

    @Override
    protected String localToString() {
        return "HDGM: "+getMagnetigHeading();
    }
}
