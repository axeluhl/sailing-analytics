package com.sap.sailing.server.gateway.serialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.WindTrackJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sse.common.TimePoint;

public class TrackedRaceJsonSerializer implements JsonSerializer<TrackedRace> {
    public static final String FIELD_AVAILABLE_WIND_SOURCES = "availableWindSources";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TYPE_NAME = "typeName";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_REGATTA = "regatta";
    public static final String FIELD_WINDSOURCES = "windSources";
    public static final String FIELD_COURSE = "course";
    public static final String FIELD_BOAT_CLASS = "boat_class";
    public static final String FIELD_COMPETITORS = "competitors";

    public static final String ALL_WINDSOURCES = "ALL";

    private String windSourceToSerialize;
    private String windSourceIdToSerialize;
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
        jsonRace.put(FIELD_REGATTA, new RegattaJsonSerializer().serialize(trackedRace.getTrackedRegatta().getRegatta()));
        jsonRace.put(FIELD_COURSE, new CourseBaseJsonSerializer(new WaypointJsonSerializer(
                                                                new ControlPointJsonSerializer(
                                                                        new MarkJsonSerializer(), 
                                                                        new GateJsonSerializer(new MarkJsonSerializer()))))
                                   .serialize(trackedRace.getRace().getCourse()));
        jsonRace.put(FIELD_BOAT_CLASS, new BoatClassJsonSerializer().serialize(trackedRace.getRace().getBoatClass()));
        
        JSONArray competitors = new JSONArray();
        for (Competitor competitor: trackedRace.getRace().getCompetitors()) {
            competitors.add(CompetitorJsonSerializer.create().serialize(competitor));
        }
        jsonRace.put(FIELD_COMPETITORS, competitors);

        if (windTrackSerializer != null) {
            JSONArray windTracks = new JSONArray();

            List<WindSource> windSources = getAvailableWindSources(trackedRace);
            JSONArray jsonWindSourcesDisplayed = new JSONArray();
            for (WindSource windSource : windSources) {
                JSONObject windSourceInformation = new JSONObject();
                windSourceInformation.put(FIELD_TYPE_NAME, windSource.getType().name());
                windSourceInformation.put(FIELD_ID, windSource.getId() != null ? windSource.getId().toString() : "");
                jsonWindSourcesDisplayed.add(windSourceInformation);
            }
            jsonRace.put(FIELD_AVAILABLE_WIND_SOURCES, jsonWindSourcesDisplayed);
            for (WindSource windSource : windSources) {
                if (ALL_WINDSOURCES.equals(windSourceToSerialize)
                        || windSource.getType().name().equalsIgnoreCase(windSourceToSerialize)) {
                    if (windSourceIdToSerialize != null && windSource.getId() != null
                            && !windSource.getId().toString().equalsIgnoreCase(windSourceIdToSerialize)) {
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

    public void setWindSourceId(String windSourceId) {
        this.windSourceIdToSerialize = windSourceId;
    }
}
