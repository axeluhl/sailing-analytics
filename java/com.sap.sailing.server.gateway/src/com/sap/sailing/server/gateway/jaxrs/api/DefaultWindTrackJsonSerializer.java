package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.jaxrs.UnitSerializationUtil;
import com.sap.sse.common.TimePoint;

public class DefaultWindTrackJsonSerializer implements WindTrackJsonSerializer {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PUBLICATION_URL = "publicationUrl";
    public static final String FIELD_VENUE = "venue";

    private TimePoint fromTime;
    private TimePoint toTime;
    private WindSource windSource;
    
    public JSONObject serialize(WindTrack windTrack) {
        JSONObject result = new JSONObject();
        JSONArray jsonWindFixes = new JSONArray();

        // quickly extract relevant fixes to hold locks as shortly as possible; process later
        ArrayList<Wind> fixes = new ArrayList<>();
        windTrack.lockForRead();
        try {
            Iterator<Wind> windIter = windTrack.getFixesIterator(fromTime, /* inclusive */true);
            while (windIter.hasNext()) {
                Wind wind = windIter.next();
                if (wind.getTimePoint().compareTo(toTime) > 0) {
                    break;
                } else {
                    fixes.add(wind);
                }
            }
        } finally {
            windTrack.unlockAfterRead();
        }

        for (Wind wind : fixes) {
            JSONObject jsonWind = new JSONObject();
            jsonWind.put("trueBearing-deg", UnitSerializationUtil.bearingDecimalFormatter.format(wind.getBearing().getDegrees()));
            jsonWind.put("speed-kts", UnitSerializationUtil.speedDecimalFormatter.format(wind.getKnots()));
            jsonWind.put("speed-m/s", UnitSerializationUtil.speedDecimalFormatter.format(wind.getMetersPerSecond()));
            if (wind.getTimePoint() != null) {
                jsonWind.put("timepoint-ms", wind.getTimePoint().asMillis());
                final Wind averagedWind = windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint());
                jsonWind.put("dampenedTrueBearing-deg", UnitSerializationUtil.bearingDecimalFormatter.format(averagedWind.getBearing().getDegrees()));
                jsonWind.put("dampenedSpeed-kts", UnitSerializationUtil.speedDecimalFormatter.format(averagedWind.getKnots()));
                jsonWind.put("dampenedSpeed-m/s", UnitSerializationUtil.speedDecimalFormatter.format(averagedWind.getMetersPerSecond()));
            }
            if (wind.getPosition() != null) {
                jsonWind.put("lat-deg", UnitSerializationUtil.latLngDecimalFormatter.format(wind.getPosition().getLatDeg()));
                jsonWind.put("lng-deg", UnitSerializationUtil.latLngDecimalFormatter.format(wind.getPosition().getLngDeg()));
            }
            jsonWindFixes.add(jsonWind);
        }
        result.put(windSource.getType() + (windSource.getId() != null ? "-"+windSource.getId().toString() : ""), jsonWindFixes);
        return result;
    }

    public void setFromTime(TimePoint fromTime) {
        this.fromTime = fromTime;
    }

    public void setToTime(TimePoint toTime) {
        this.toTime = toTime;
    }

    public void setWindSource(WindSource windSource) {
        this.windSource = windSource;
    }
}