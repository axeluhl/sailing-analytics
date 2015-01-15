package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.igtimiadapter.IgtimiFixReceiver;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sse.common.TimePoint;

public class GpsLatLong extends Fix {
    public static final String IGTIMI_ENABLE_WORKAROUND_FOR_SINGLE_DIGIT_LATITUDES = "igtimi.enableWorkaroundForSingleDigitLatitudes";
    private static final long serialVersionUID = 5056284867725893553L;
    private final Position position;
    
    public GpsLatLong(TimePoint timePoint, Sensor sensor, Map<Integer, Object> valuesPerSubindex) {
        super(sensor, timePoint);
        final double longitudeInDegrees = ((Number) valuesPerSubindex.get(1)).doubleValue();
        final double preliminaryLatitudeInDegrees = ((Number) valuesPerSubindex.get(2)).doubleValue();
        final double latitudeInDegrees;
        if (Boolean.valueOf(System.getProperty(IGTIMI_ENABLE_WORKAROUND_FOR_SINGLE_DIGIT_LATITUDES, "false"))) {
            final int degrees = (int) preliminaryLatitudeInDegrees;
            final double minutes = (preliminaryLatitudeInDegrees - degrees)*60.0;
            if (Math.abs(minutes) >= 10) {
                // the minutes have two digits; cannot have been a mis-parse
                latitudeInDegrees = preliminaryLatitudeInDegrees;
            } else {
                final double correctedMinutes = minutes + 10.0*(degrees%10);
                latitudeInDegrees = ((int) (degrees/10)) + correctedMinutes/60.0;
            }
        } else {
            latitudeInDegrees = preliminaryLatitudeInDegrees;
        }
        position = new DegreePosition(latitudeInDegrees, longitudeInDegrees);
    }

    public Position getPosition() {
        return position;
    }

    @Override
    protected String localToString() {
        return position.toString();
    }

    @Override
    public void notify(IgtimiFixReceiver receiver) {
        receiver.received(this);
    }
}
