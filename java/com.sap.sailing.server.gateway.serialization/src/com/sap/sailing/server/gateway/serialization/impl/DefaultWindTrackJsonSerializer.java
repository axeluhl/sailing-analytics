package com.sap.sailing.server.gateway.serialization.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.deserialization.impl.WindTrackJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.WindTrackJsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.util.RoundingUtil;

public class DefaultWindTrackJsonSerializer implements WindTrackJsonSerializer {
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_DAMPENED_SPEED_M_S = "dampenedSpeed-m/s";
    public static final String FIELD_DAMPENED_SPEED_KTS = "dampenedSpeed-kts";
    public static final String FIELD_DAMPENED_BEARING_DEG = "dampenedTrueBearing-deg";
    public static final String FIELD_SPEED_M_S = "speed-m/s";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PUBLICATION_URL = "publicationUrl";
    public static final String FIELD_VENUE = "venue";

    private TimePoint fromTime;
    private TimePoint toTime;
    private WindSource windSource;
    
    /**
     * -1 means unlimited.
     */
    private final int maxNumberOfFixes;
    
    public DefaultWindTrackJsonSerializer(int maxNumberOfFixes) {
        super();
        this.maxNumberOfFixes = maxNumberOfFixes;
    }
    
    public DefaultWindTrackJsonSerializer() {
        this(-1);
    }

    public JSONObject serialize(WindTrack windTrack) {
        JSONObject result = new JSONObject();
        JSONArray jsonWindFixes = new JSONArray();

        // quickly extract relevant fixes to hold locks as shortly as possible; process later
        ArrayList<Wind> fixes = new ArrayList<>();
        windTrack.lockForRead();
        try {
            Iterator<Wind> windIter = windTrack.getFixesIterator(fromTime, /* inclusive */true, toTime, /* inclusive */ false);
            int count = 0;
            while ((maxNumberOfFixes == -1 || count<maxNumberOfFixes) && windIter.hasNext()) {
                fixes.add(windIter.next());
            }
        } finally {
            windTrack.unlockAfterRead();
        }

        for (Wind wind : fixes) {
            JSONObject jsonWind = new JSONObject();
            jsonWind.put(WindTrackJsonDeserializer.FIELD_BEARING_DEG, RoundingUtil.bearingDecimalFormatter.format(wind.getBearing().getDegrees()));
            jsonWind.put(WindTrackJsonDeserializer.FIELD_SPEED_KTS, RoundingUtil.speedDecimalFormatter.format(wind.getKnots()));
            jsonWind.put(FIELD_SPEED_M_S, RoundingUtil.speedDecimalFormatter.format(wind.getMetersPerSecond()));
            if (wind.getTimePoint() != null) {
                jsonWind.put(WindTrackJsonDeserializer.FIELD_TIMEPOINT, wind.getTimePoint().asMillis());
                final Wind averagedWind = windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint());
                jsonWind.put(FIELD_DAMPENED_BEARING_DEG, RoundingUtil.bearingDecimalFormatter.format(averagedWind.getBearing().getDegrees()));
                jsonWind.put(FIELD_DAMPENED_SPEED_KTS, RoundingUtil.speedDecimalFormatter.format(averagedWind.getKnots()));
                jsonWind.put(FIELD_DAMPENED_SPEED_M_S, RoundingUtil.speedDecimalFormatter.format(averagedWind.getMetersPerSecond()));
            }
            if (wind.getPosition() != null) {
                jsonWind.put(WindTrackJsonDeserializer.FIELD_LAT_DEG, RoundingUtil.latLngDecimalFormatter.format(wind.getPosition().getLatDeg()));
                jsonWind.put(WindTrackJsonDeserializer.FIELD_LNG_DEG, RoundingUtil.latLngDecimalFormatter.format(wind.getPosition().getLngDeg()));
            }
            jsonWindFixes.add(jsonWind);
        }
        result.put(windSource.getType() + (windSource.getId() != null ? "-"+windSource.getId().toString() : ""), jsonWindFixes);
        result.put(FIELD_SOURCE_TYPE, windSource.getType().name());
        result.put(WindTrackJsonDeserializer.FIELD_MILLISECONDS_OVER, windTrack.getMillisecondsOverWhichToAverageWind());
        result.put(WindTrackJsonDeserializer.FIELD_USE_SPEED, windTrack.isUseSpeed());
        result.put(WindTrackJsonDeserializer.FIELD_NAME_FOR_LOCK, windTrack.getNameForReadWriteLock());
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