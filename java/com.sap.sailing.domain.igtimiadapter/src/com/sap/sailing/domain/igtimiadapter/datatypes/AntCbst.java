package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * Cadence value measured by an ANT sensor
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AntCbst extends Fix {
    private final int cadence;
    
    public AntCbst(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        cadence = ((Number) valuesPerSubindex.get(1)).intValue();
    }

    public int getCadence() {
        return cadence;
    }
}
