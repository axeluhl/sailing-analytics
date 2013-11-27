package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;

/**
 * A heart rate measurement received through a sensor supporting the ANT protocol
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AntHrm extends Fix {
    /**
     * The ANT protocol specifies its own time stamp that may vary from the {@link #getTimePoint transmitter's time stamp}. 
     */
    private final TimePoint eventTime;
    private final int heartBeatCount;
    private final int heartRate;
    
    public AntHrm(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        eventTime = new MillisecondsTimePoint(((Number) valuesPerSubindex.get(1)).longValue());
        heartBeatCount = ((Number) valuesPerSubindex.get(2)).intValue();
        heartRate = ((Number) valuesPerSubindex.get(3)).intValue();
    }

    public TimePoint getEventTime() {
        return eventTime;
    }

    public int getHeartBeatCount() {
        return heartBeatCount;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
