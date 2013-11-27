package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.igtimiadapter.Sensor;

public class GpsLatLong extends Fix {
    private final Position position;
    
    public GpsLatLong(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        position = new DegreePosition(((Number) valuesPerSubindex.get(1)).doubleValue(), ((Number) valuesPerSubindex.get(2)).doubleValue());
    }

    public Position getPosition() {
        return position;
    }

    @Override
    protected String localToString() {
        return position.toString();
    }
}
