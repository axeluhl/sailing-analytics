package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiver;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sse.common.TimePoint;

/**
 * A heart rate measurement received through a sensor supporting the ANT protocol
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AntHrm extends Fix {
    private static final long serialVersionUID = 94260324289901500L;
    private final int eventTime;
    private final int heartBeatCount;
    private final int heartRate;
    
    public AntHrm(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        eventTime = ((Number) valuesPerSubindex.get(1)).intValue();
        heartBeatCount = ((Number) valuesPerSubindex.get(2)).intValue();
        heartRate = ((Number) valuesPerSubindex.get(3)).intValue();
    }

    public int getEventTime() {
        return eventTime;
    }

    public int getHeartBeatCount() {
        return heartBeatCount;
    }

    public int getHeartRate() {
        return heartRate;
    }

    @Override
    protected String localToString() {
        return "heart rate "+getHeartRate()+"/min";
    }

    @Override
    public void notify(IgtimiFixReceiver receiver) {
        receiver.received(this);
    }
}
