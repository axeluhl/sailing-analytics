package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * A heart rate measurement received through a sensor supporting the ANT protocol
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AntHrm extends Fix {
    private final int heartRate;
    
    public AntHrm(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        heartRate = ((Number) valuesPerSubindex.get(1)).intValue();
    }

    public int getHeartRate() {
        return heartRate;
    }
}
