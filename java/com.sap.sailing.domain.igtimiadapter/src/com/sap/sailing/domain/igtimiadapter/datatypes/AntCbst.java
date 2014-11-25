package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiver;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sse.common.TimePoint;

/**
 * Cadence value measured by an ANT sensor
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AntCbst extends Fix {
    private static final long serialVersionUID = 7696345613317022955L;
    private final int cadence;
    
    public AntCbst(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        cadence = ((Number) valuesPerSubindex.get(1)).intValue();
    }

    public int getCadence() {
        return cadence;
    }
    
    @Override
    public String localToString() {
        return "Cadence "+getCadence();
    }

    @Override
    public void notify(IgtimiFixReceiver receiver) {
        receiver.received(this);
    }
}
