package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;

public class TrackedRaceJsonSerializer implements JsonSerializer<TrackedRace> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_REGATTA = "regatta";
    public static final String FIELD_WINDSOURCES = "windSources";

    public static final String ALL_WINDSOURCES = "ALL";

    private String windSourceToSerialize;
    private TimePoint fromTime;
    private TimePoint toTime;
    private final WindTrackJsonSerializer windTrackSerializer;

    public TrackedRaceJsonSerializer(WindTrackJsonSerializer windTrackSerializer) {
        this.windTrackSerializer = windTrackSerializer;
        windSourceToSerialize = WindSourceType.COMBINED.name();
    }

    public JSONObject serialize(TrackedRace trackedRace) {
        JSONObject jsonRace = new JSONObject();
        
        jsonRace.put(FIELD_NAME, trackedRace.getRace().getName());
        jsonRace.put(FIELD_REGATTA, trackedRace.getRaceIdentifier().getRegattaName());

        if(windTrackSerializer != null) {
            JSONArray windTracks = new JSONArray();

            List<WindSource> windSources = getAvailableWindSources(trackedRace);
            for (WindSource windSource : windSources) {
                if (ALL_WINDSOURCES.equals(windSourceToSerialize) || windSource.getType().name().equalsIgnoreCase(windSourceToSerialize)) {
                    windTrackSerializer.setFromTime(fromTime);
                    windTrackSerializer.setToTime(toTime);
                    windTrackSerializer.setWindSource(windSource);
                    
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    JSONObject jsonWindTrack = windTrackSerializer.serialize(windTrack);
                    windTracks.add(jsonWindTrack);
                }
            }
            jsonRace.put(FIELD_WINDSOURCES, windTracks);
        }

        return jsonRace;
    }

    public void setWindSource(String windSourceToSerialize) {
        this.windSourceToSerialize = windSourceToSerialize;
    }

    public void setFromTime(TimePoint fromTime) {
        this.fromTime = fromTime;
    }

    public void setToTime(TimePoint toTime) {
        this.toTime = toTime;
    }
    
    private List<WindSource> getAvailableWindSources(TrackedRace trackedRace) {
        List<WindSource> windSources = new ArrayList<WindSource>();
        for (WindSource windSource : trackedRace.getWindSources()) {
            windSources.add(windSource);
        }
        for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
            windSources.remove(windSourceToExclude);
        }
        windSources.add(new WindSourceImpl(WindSourceType.COMBINED));
        return windSources;
    }   
}