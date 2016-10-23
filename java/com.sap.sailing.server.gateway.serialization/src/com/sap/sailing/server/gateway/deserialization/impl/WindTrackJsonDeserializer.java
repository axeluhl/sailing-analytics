package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindTrackJsonDeserializer implements JsonDeserializer<WindTrack> {

    public static final String FIELD_BEARING_DEG = "trueBearing-deg";
    public static final String FIELD_SPEED_KTS = "speed-kts";
    public static final String FIELD_TIMEPOINT = "timepoint-ms";
    public static final String FIELD_LNG_DEG = "lng-deg";
    public static final String FIELD_LAT_DEG = "lat-deg";
    public static final String FIELD_NAME_FOR_LOCK = "nameForReadWriteLock";
    public static final String FIELD_USE_SPEED = "useSpeed";
    public static final String FIELD_MILLISECONDS_OVER = "millisecondsOverWhichToAverage";
    
    private final String windSourceToDeserialize;

    public WindTrackJsonDeserializer(String windSourceToDeserialize) {
        this.windSourceToDeserialize = windSourceToDeserialize;
    }

    @Override
    public WindTrack deserialize(JSONObject object) throws JsonDeserializationException {
        long millisecondsOverWhichToAverage = (long) object.get(FIELD_MILLISECONDS_OVER);
        boolean useSpeed = (boolean) object.get(FIELD_USE_SPEED);
        String nameForReadWriteLock = (String) object.get(FIELD_NAME_FOR_LOCK);
        WindTrack windTrack = new WindTrackImpl(millisecondsOverWhichToAverage, useSpeed, nameForReadWriteLock);

        JSONArray fixes = (JSONArray) object.get(windSourceToDeserialize);

        for (int i = 0; i < fixes.size(); i++) {
            JSONObject fix = (JSONObject) fixes.get(i);

            Position position = null;
            if (fix.get(FIELD_LAT_DEG) != null && fix.get(FIELD_LNG_DEG) != null) {
                double latDeg = (double) fix.get(FIELD_LAT_DEG);
                double lngDeg = (double) fix.get(FIELD_LNG_DEG);
                position = new DegreePosition(latDeg, lngDeg);
            }

            TimePoint timePoint = null;
            if (fix.get(FIELD_TIMEPOINT) != null) {
                timePoint = new MillisecondsTimePoint((long) fix.get(FIELD_TIMEPOINT));
            }

            SpeedWithBearing speedWithBearing = null;
            if (fix.get(FIELD_SPEED_KTS) != null && fix.get(FIELD_BEARING_DEG) != null) {
                speedWithBearing = new KnotSpeedWithBearingImpl((double) fix.get(FIELD_SPEED_KTS),
                        new DegreeBearingImpl((double) fix.get(FIELD_BEARING_DEG)));
            }

            Wind wind = new WindImpl(position, timePoint, speedWithBearing);
            windTrack.add(wind);
        }

        return windTrack;
    }
}