package com.sap.sailing.server.gateway.serialization.impl;

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

    private final String windSourceToSerialize;
    private final String windSourceIdToSerialize;
    private final TimePoint fromTime;
    private final TimePoint toTime;
    private final WindTrackJsonSerializer windTrackSerializer;

    public TrackedRaceJsonSerializer(WindTrackJsonSerializer windTrackSerializer, String windSourceToSerialize, String windSourceIdToSerialize, TimePoint fromTime, TimePoint toTime) {
        this.windTrackSerializer = windTrackSerializer;
        this.windSourceToSerialize = windSourceToSerialize;
        this.windSourceIdToSerialize = windSourceIdToSerialize;
        this.fromTime = fromTime;
        this.toTime = toTime;
        windSourceToSerialize = WindSourceType.COMBINED.name();
    }

    public JSONObject serialize(TrackedRace trackedRace) {
        JSONObject jsonRace = new JSONObject();
        
        jsonRace.put(FIELD_NAME, trackedRace.getRace().getName());
        jsonRace.put(FIELD_REGATTA, trackedRace.getRaceIdentifier().getRegattaName());

        if(windTrackSerializer != null) {
            JSONArray windTracks = new JSONArray();

            List<WindSource> windSources = getAvailableWindSources(trackedRace);
            JSONArray jsonWindSourcesDisplayed = new JSONArray();
            for (WindSource windSource : windSources) {
                JSONObject windSourceInformation = new JSONObject();
                windSourceInformation.put("typeName", windSource.getType().name());
                windSourceInformation.put("id", windSource.getId() != null ? windSource.getId().toString() : "");
                jsonWindSourcesDisplayed.add(windSourceInformation);
            }
            jsonRace.put("availableWindSources", jsonWindSourcesDisplayed);
            for (WindSource windSource : windSources) {
                if (ALL_WINDSOURCES.equals(windSourceToSerialize) || windSource.getType().name().equalsIgnoreCase(windSourceToSerialize)) {
                    if (windSourceIdToSerialize != null && windSource.getId() != null && !windSource.getId().toString().equalsIgnoreCase(windSourceIdToSerialize)) {
                        continue;
                    }
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

    private List<WindSource> getAvailableWindSources(TrackedRace trackedRace) {
        List<WindSource> windSources = new ArrayList<WindSource>();
        for (WindSource windSource : trackedRace.getWindSources()) {
            windSources.add(windSource);
        }
        for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
            windSources.remove(windSourceToExclude);
        }
        windSources.add(new WindSourceImpl(WindSourceType.COMBINED));
        for (final WindSource trackedLegMiddleWindSource : trackedRace.getWindSources(WindSourceType.LEG_MIDDLE)) {
            windSources.add(trackedLegMiddleWindSource);
        }
        return windSources;
    }
}