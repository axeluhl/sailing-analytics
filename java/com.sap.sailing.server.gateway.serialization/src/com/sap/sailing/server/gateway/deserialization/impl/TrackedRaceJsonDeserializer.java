package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRegattaImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseBaseDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;

public class TrackedRaceJsonDeserializer implements JsonDeserializer<TrackedRace> {
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_BOAT_CLASS = "boat_class";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COURSE = "course";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_ID = "id";
    public static final String FIELD_REGATTA = "regatta";
    public static final String FIELD_WINDSOURCES = "windSources";

    private final WindTrackJsonDeserializer windTrackDeserializer;

    public TrackedRaceJsonDeserializer() {
        this.windTrackDeserializer = new WindTrackJsonDeserializer(WindSourceType.COMBINED.name());
    }

    public TrackedRace deserialize(JSONObject object) throws JsonDeserializationException {
        SharedDomainFactory factory = DomainFactory.INSTANCE;

        List<Competitor> competitors = new ArrayList<>();
        JSONArray competitiorsJSON = (JSONArray) object.get(FIELD_COMPETITORS);
        for (int i = 0; i < competitiorsJSON.size(); i++) {
            JSONObject competitorJSON = (JSONObject) competitiorsJSON.get(i);
            competitors.add(CompetitorJsonDeserializer.create(factory).deserialize(competitorJSON));
        }

        CourseDataImpl course = (CourseDataImpl) new CourseBaseDeserializer(
                new WaypointDeserializer(new ControlPointDeserializer(new MarkDeserializer(factory),
                        new GateDeserializer(factory, new MarkDeserializer(factory)))))
                                .deserialize((JSONObject) object.get(FIELD_COURSE));

        RaceDefinition raceDefinition = new RaceDefinitionImpl((String) object.get(FIELD_NAME),
                new CourseImpl(course.getName(), course.getWaypoints()),
                new BoatClassJsonDeserializer(factory).deserialize((JSONObject) object.get(FIELD_BOAT_CLASS)), competitors);

        TrackedRegatta trackedRegatta = new TrackedRegattaImpl(
                new RegattaJsonDeserializer().deserialize((JSONObject) object.get(FIELD_REGATTA)));

        JSONArray windSources = (JSONArray) object.get(FIELD_WINDSOURCES);
        Map<WindSource, WindTrack> map = new HashMap<>();
        for (int i = 0; i < windSources.size(); i++) {
            JSONObject windSource = (JSONObject) windSources.get(i);
            String sourceType = (String) windSource.get(FIELD_SOURCE_TYPE);
            WindSource source = new WindSourceImpl(WindSourceType.valueOf(sourceType));
            WindTrack windTrack = windTrackDeserializer.deserialize(windSource);
            map.put(source, windTrack);
        }

        WindStore windStore = new EmptyWindStore() {

            @Override
            public WindTrack getWindTrack(String regattaName, TrackedRace trackedRace, WindSource windSource,
                    long millisecondsOverWhichToAverage, long delayForWindEstimationCacheInvalidation) {
                WindTrack windTrack = map.get(windSource);

                if (windTrack == null) {
                    return super.getWindTrack(regattaName, trackedRace, windSource, millisecondsOverWhichToAverage,
                            delayForWindEstimationCacheInvalidation);
                }

                return windTrack;
            }
        };

        return new DynamicTrackedRaceImpl(trackedRegatta, raceDefinition, Collections.<Sideline> emptyList(), windStore,
                /* delayToLiveInMillis */ 0, /* millisecondsOverWhichToAverageWind */ 30000,
                /* millisecondsOverWhichToAverageSpeed */ 30000, /* delay for wind estimation cache invalidation */ 0,
                false, OneDesignRankingMetric::new, factory.getRaceLogResolver());
    }

}
