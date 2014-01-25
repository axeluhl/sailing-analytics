package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.masterdataimport.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.RaceColumnMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.ScoreCorrectionMasterData;
import com.sap.sailing.domain.masterdataimport.SingleScoreCorrectionMasterData;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardMasterDataJsonSerializer;

public class LeaderboardMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardMasterData> {

    private final JsonDeserializer<Competitor> competitorDeserializer;
    private final DomainFactory domainFactory;
    private final JsonDeserializer<RaceLogEvent> raceLogEventDeseriaizer;
    private final JsonDeserializer<RaceColumnMasterData> raceColumnDeserializer = new RaceColumnMasterDataJsonDeserializer();

    public LeaderboardMasterDataJsonDeserializer(JsonDeserializer<Competitor> competitorDeserializer,
            DomainFactory domainFactory, JsonDeserializer<RaceLogEvent> raceLogEventDeseriaizer) {
        this.competitorDeserializer = competitorDeserializer;
        this.domainFactory = domainFactory;
        this.raceLogEventDeseriaizer = raceLogEventDeseriaizer;
    }

    @Override
    public LeaderboardMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        if (object == null) {
            return null;
        }
        String name = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_NAME);
        String displayName = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_DISPLAY_NAME);
        Map<Serializable, String> displayNamesByCompetitorId = deserializeCompetitorDisplayNames((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_DISPLAY_NAMES));
        ScoreCorrectionMasterData scoreCorrection = deserializeScoreCorrection((JSONObject) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_SCORE_CORRECTION));
        Map<Serializable, Double> carriedPoints = deserializeCarriedPoints((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_CARRIED_POINTS));
        List<Serializable> suppressedCompetitors = deserializeSuppressedCompetitors((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_SUPPRESSED_COMPETITORS));
        Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents = deserializeRaceLogEvents((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_LOG_EVENTS));
        int[] resultDiscardingRule = deserializeResultDesicardingRule((JSONObject) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_RESULT_DISCARDING_RULE));
        boolean isRegattaLeaderBoard = (Boolean) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_LEADERBOARD);

        Map<Serializable, Competitor> competitorsById = new HashMap<Serializable, Competitor>();
        JSONArray competitorsJsonArray = (JSONArray) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITORS);

        for (Object obj : competitorsJsonArray) {
            JSONObject competitorJson = (JSONObject) obj;
            Competitor deserializedCompetitor = competitorDeserializer.deserialize(competitorJson);
            competitorsById.put(deserializedCompetitor, deserializedCompetitor);
        }
        final LeaderboardMasterData result;
        if (isRegattaLeaderBoard) {
            String regattaName = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_NAME);
            result = new RegattaLeaderboardMasterData(name, displayName, resultDiscardingRule, competitorsById,
                    scoreCorrection, regattaName, carriedPoints, suppressedCompetitors, displayNamesByCompetitorId,
                    raceLogEvents);

        } else {
            ScoringScheme scoringScheme = deserializeScoringScheme((JSONObject) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_SCORING_SCHEME), domainFactory);
            String courseAreaId = deserializeCourseAreaId((JSONObject) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_COURSE_AREA));
            List<RaceColumnMasterData> raceColumns = deserializeRaceColumns((JSONArray) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_COLUMNS));
            result = new FlexibleLeaderboardMasterData(name, displayName, resultDiscardingRule, competitorsById,
                    scoreCorrection, scoringScheme, courseAreaId, raceColumns, carriedPoints, suppressedCompetitors,
                    displayNamesByCompetitorId, raceLogEvents);
        }
        return result;
    }

    private Map<String, Map<String, List<RaceLogEvent>>> deserializeRaceLogEvents(JSONArray jsonArray) {
        Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents = new HashMap<String, Map<String, List<RaceLogEvent>>>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            String raceColumnName = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_COLUMN_NAME);
            Map<String, List<RaceLogEvent>> perFleet = deserializeRaceLogEventsForFleets((JSONArray) json
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_FLEETS));
            raceLogEvents.put(raceColumnName, perFleet);
        }
        return raceLogEvents;
    }

    private Map<String, List<RaceLogEvent>> deserializeRaceLogEventsForFleets(JSONArray fleets) {
        Map<String, List<RaceLogEvent>> perFleet = new HashMap<String, List<RaceLogEvent>>();
        for (Object obj : fleets) {
            JSONObject json = (JSONObject) obj;
            String fleetName = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_FLEET_NAME);
            List<RaceLogEvent> logEvents = deserializeRaceLogEventArray((JSONArray) json
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_LOG_EVENTS));
            perFleet.put(fleetName, logEvents);
        }
        return perFleet;
    }

    private List<RaceLogEvent> deserializeRaceLogEventArray(JSONArray jsonArray) {
        List<RaceLogEvent> logEvents = new ArrayList<RaceLogEvent>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            try {
                logEvents.add(raceLogEventDeseriaizer.deserialize(json));
            } catch (JsonDeserializationException e) {
                // TODO Exception handling
                e.printStackTrace();
            }
        }
        return logEvents;
    }

    private Map<Serializable, String> deserializeCompetitorDisplayNames(JSONArray jsonArray) {
        Map<Serializable, String> result = new HashMap<Serializable, String>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            Serializable id;
            try {
                JSONObject object = (JSONObject) obj;
                id = extractId(object);
            } catch (Exception e) {
                // Backward compability
                id = (String) obj;
            }
            String displayName = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_DISPLAY_NAME);
            result.put(id, displayName);
        }
        return result;
    }

    public static List<Serializable> deserializeSuppressedCompetitors(JSONArray jsonArray) {
        List<Serializable> ids = new ArrayList<Serializable>();
        if (jsonArray != null) {
            for (Object obj : jsonArray) {
                Serializable id;
                try {
                    JSONObject object = (JSONObject) obj;
                    id = extractId(object);
                } catch (Exception e) {
                    // Backward compability
                    id = (String) obj;
                }
                ids.add(id);
            }
        }
        return ids;
    }

    private static Serializable extractId(JSONObject object) {
        Serializable id;
        Object idClassName = object.get(LeaderboardMasterDataJsonSerializer.FIELD_TYPE);
        id = (Serializable) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_ID);
        if (idClassName != null) {
            try {
                Class<?> idClass = Class.forName((String) idClassName);
                if (Number.class.isAssignableFrom(idClass)) {
                    Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                    id = (Serializable) constructorFromString.newInstance(id.toString());
                } else if (UUID.class.isAssignableFrom(idClass)) {
                    id = Helpers.tryUuidConversion(id);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // To ensure some kind of a backward compability.
            try {
                id = UUID.fromString(id.toString());
            } catch (IllegalArgumentException e) {
                id = id.toString();
            }
        }
        return id;
    }

    private Map<Serializable, Double> deserializeCarriedPoints(JSONArray jsonArray) {
        Map<Serializable, Double> carriedPoints = new HashMap<Serializable, Double>();
        for (Object obj : jsonArray) {
            JSONObject jsonCarriedPoints = (JSONObject) obj;
            Serializable id;
            try {
                id = extractId(jsonCarriedPoints);
            } catch (Exception e) {
                // Backward compability
                id = (String) obj;
            }
            Double carried = (Double) jsonCarriedPoints.get(LeaderboardMasterDataJsonSerializer.FIELD_CARRIED);
            carriedPoints.put(id, carried);
        }
        return carriedPoints;
    }

    private ScoreCorrectionMasterData deserializeScoreCorrection(JSONObject jsonObject) {
        String comment = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_COMMENT);
        Object timeObject = jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_TIME_POINT);
        Long timepointMillis = null;
        if (timeObject != null) {
            timepointMillis = (Long) timeObject;
        }

        JSONArray forRaceColumns = (JSONArray) jsonObject
                .get(LeaderboardMasterDataJsonSerializer.FIELD_FOR_RACE_COLUMNS);
        Map<String, Iterable<SingleScoreCorrectionMasterData>> correctionForRaceColumns = new HashMap<String, Iterable<SingleScoreCorrectionMasterData>>();
        for (Object obj : forRaceColumns) {
            JSONObject json = (JSONObject) obj;
            String raceColumnName = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_COLUMN_NAME);
            Iterable<SingleScoreCorrectionMasterData> correctionsForRaceColumn = deserializeScoreCorrectionForRaceColumn((JSONArray) json
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_FOR_COMPETITORS));
            correctionForRaceColumns.put(raceColumnName, correctionsForRaceColumn);
        }
        return new ScoreCorrectionMasterData(comment, timepointMillis, correctionForRaceColumns);
    }

    private Iterable<SingleScoreCorrectionMasterData> deserializeScoreCorrectionForRaceColumn(JSONArray jsonArray) {
        Set<SingleScoreCorrectionMasterData> result = new HashSet<SingleScoreCorrectionMasterData>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            result.add(deserializeScoreCorrectionForCompetitor(json));
        }
        return result;
    }

    private SingleScoreCorrectionMasterData deserializeScoreCorrectionForCompetitor(JSONObject json) {
        Serializable competitorId = extractId(json);
        Double explicitScoreCorrection = ((Double) json
                .get(LeaderboardMasterDataJsonSerializer.FIELD_EXPLICIT_SCORE_CORRECTION));
        String maxPointsReason = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_MAX_POINTS_REASON);
        return new SingleScoreCorrectionMasterData(competitorId, explicitScoreCorrection, maxPointsReason);
    }

    private List<RaceColumnMasterData> deserializeRaceColumns(JSONArray array) throws JsonDeserializationException {
        List<RaceColumnMasterData> columns = new ArrayList<RaceColumnMasterData>();
        for (Object columnObj : array) {
            JSONObject columnJson = (JSONObject) columnObj;
            columns.add(raceColumnDeserializer.deserialize(columnJson));
        }
        return columns;
    }

    private String deserializeCourseAreaId(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String id = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_ID);
        return id;

    }

    public static ScoringScheme deserializeScoringScheme(JSONObject jsonObject, DomainFactory domainFactory) {
        final ScoringScheme result;
        if (jsonObject == null) {
            result = null;
        } else {
            String type = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_TYPE);
            if (type == null) {
                result = null;
            } else {
                result = domainFactory.createScoringScheme(ScoringSchemeType.valueOf(type));
            }
        }
        return result;
    }
    
    public static int[] deserializeResultDesicardingRule(JSONObject jsonObject) {
        final int[] result;
        if (jsonObject == null) {
            result = null;
        } else {
            JSONArray indeces = (JSONArray) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_INDICES);
            if (indeces == null) {
                result = null;
            } else {
                result = new int[indeces.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = ((Long) indeces.get(i)).intValue();
                }
            }
        }
        return result;
    }
}
