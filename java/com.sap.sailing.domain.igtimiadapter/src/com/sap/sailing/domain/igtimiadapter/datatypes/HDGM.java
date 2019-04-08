package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiver;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;

/**
 * Magnetic heading
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class HDGM extends Fix {
    private static final long serialVersionUID = -3344091983385126284L;
    private final Bearing magnetigHeading;
    
    public HDGM(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        magnetigHeading = new DegreeBearingImpl((((Number) valuesPerSubindex.get(1)).doubleValue()+360.)%360.);
    }

    public Bearing getMagnetigHeading() {
        return magnetigHeading;
    }

    @Override
    protected String localToString() {
        return "HDGM: "+getMagnetigHeading();
    }

    @Override
    public void notify(IgtimiFixReceiver receiver) {
        receiver.received(this);
    }
}
