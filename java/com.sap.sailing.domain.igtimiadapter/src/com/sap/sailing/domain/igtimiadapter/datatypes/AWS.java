package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiver;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * Apparent wind speed, relative to the inertial system on which the wind speed was measured
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AWS extends Fix {
    private static final long serialVersionUID = -4044229400122127582L;
    private final Speed apparentWindSpeed;
    
    public AWS(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        apparentWindSpeed = new KnotSpeedImpl(((Number) valuesPerSubindex.get(1)).doubleValue());
    }

    public Speed getApparentWindSpeed() {
        return apparentWindSpeed;
    }

    @Override
    protected String localToString() {
        return "AWS: "+getApparentWindSpeed();
    }

    @Override
    public void notify(IgtimiFixReceiver receiver) {
        receiver.received(this);
    }
}
