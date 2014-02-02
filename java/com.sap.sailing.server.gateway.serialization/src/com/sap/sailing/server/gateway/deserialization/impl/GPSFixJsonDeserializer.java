package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class GPSFixJsonDeserializer implements
JsonDeserializer<GPSFix> {

    public static final String FIELD_LAT_DEG = "lat_deg";
    public static final String FIELD_LON_DEG = "lon_deg";
    public static final String FIELD_TIME = "unixtime";


    @Override
    public GPSFix deserialize(JSONObject object)
            throws JsonDeserializationException {
        Date time = new Date((Long) object.get(FIELD_TIME));
        double latDeg = (Double) object.get(FIELD_LAT_DEG);
        double lonDeg = (Double) object.get(FIELD_LON_DEG);

        Position position = new DegreePosition(latDeg, lonDeg);
        TimePoint timePoint = new MillisecondsTimePoint(time);

        GPSFix fix = new GPSFixImpl(position, timePoint);

        return fix;
    }

}
