package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.masterdataimport.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.masterdataimport.ScoreCorrectionMasterData;
import com.sap.sailing.domain.masterdataimport.SingleScoreCorrectionMasterData;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardMasterDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.RaceColumnMasterDataJsonSerializer;

public class LeaderboardMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardMasterData> {

    private final JsonDeserializer<Competitor> competitorDeserializer;
    private final DomainFactory domainFactory;
    private final JsonDeserializer<RaceLogEvent> raceLogEventDeseriaizer;

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
        Map<String, String> displayNamesByCompetitorId = deserializeCompetitorDisplayNames((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_DISPLAY_NAMES));
        ScoreCorrectionMasterData scoreCorrection = deserializeScoreCorrection((JSONObject) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_SCORE_CORRECTION));
        Map<String, Double> carriedPoints = deserializeCarriedPoints((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_CARRIED_POINTS));
        List<String> suppressedCompetitors = deserializeSuppressedCompetitors((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_SUPPRESSED_COMPETITORS));
        Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents = deserializeRaceLogEvents((JSONArray) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_LOG_EVENTS));
        int[] resultDiscardingRule = deserializeResultDesicardingRule((JSONObject) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_RESULT_DISCARDING_RULE));
        boolean isRegattaLeaderBoard = (Boolean) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_LEADERBOARD);

        Map<String, Competitor> competitorsById = new HashMap<String, Competitor>();
        JSONArray competitorsJsonArray = (JSONArray) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITORS);

        for (Object obj : competitorsJsonArray) {
            JSONObject competitorJson = (JSONObject) obj;
            Competitor deserializedCompetitor = competitorDeserializer.deserialize(competitorJson);
            competitorsById.put(deserializedCompetitor.getId().toString(), deserializedCompetitor);
        }
        if (isRegattaLeaderBoard) {
            String regattaName = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_NAME);
            return new RegattaLeaderboardMasterData(name, displayName, resultDiscardingRule, competitorsById,
                    scoreCorrection, regattaName, carriedPoints, suppressedCompetitors, displayNamesByCompetitorId,
                    raceLogEvents);

        } else {
            ScoringScheme scoringScheme = deserializeScoringScheme((JSONObject) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_SCORING_SCHEME));
            String courseAreaId = deserializeCourseAreaId((JSONObject) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_COURSE_AREA));
            List<Pair<String, Boolean>> raceColumns = deserializeRaceColumns((JSONArray) object
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_RACE_COLUMNS));
            return new FlexibleLeaderboardMasterData(name, displayName, resultDiscardingRule, competitorsById,
                    scoreCorrection, scoringScheme, courseAreaId, raceColumns, carriedPoints, suppressedCompetitors,
                    displayNamesByCompetitorId, raceLogEvents);
        }
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

    private Map<String, String> deserializeCompetitorDisplayNames(JSONArray jsonArray) {
        Map<String, String> result = new HashMap<String, String>();
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            String id = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_ID);
            String displayName = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_DISPLAY_NAME);
            result.put(id, displayName);
        }
        return result;
    }

    private List<String> deserializeSuppressedCompetitors(JSONArray jsonArray) {
        List<String> ids = new ArrayList<String>();
        for (Object obj : jsonArray) {
            ids.add((String) obj);
        }
        return ids;
    }

    private Map<String, Double> deserializeCarriedPoints(JSONArray jsonArray) {
        Map<String, Double> carriedPoints = new HashMap<String, Double>();
        for (Object obj : jsonArray) {
            JSONObject jsonCarriedPoints = (JSONObject) obj;
            String competitorId = (String) jsonCarriedPoints
                    .get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_ID);
            Double carried = (Double) jsonCarriedPoints.get(LeaderboardMasterDataJsonSerializer.FIELD_CARRIED);
            carriedPoints.put(competitorId, carried);
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
        String competitorId = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITOR_ID);
        Double explicitScoreCorrection = ((Double) json
                .get(LeaderboardMasterDataJsonSerializer.FIELD_EXPLICIT_SCORE_CORRECTION));
        String maxPointsReason = (String) json.get(LeaderboardMasterDataJsonSerializer.FIELD_MAX_POINTS_REASON);
        return new SingleScoreCorrectionMasterData(competitorId, explicitScoreCorrection, maxPointsReason);
    }

    private List<Pair<String, Boolean>> deserializeRaceColumns(JSONArray array) {
        List<Pair<String, Boolean>> columns = new ArrayList<Pair<String, Boolean>>();
        for (Object columnObj : array) {
            JSONObject columnJson = (JSONObject) columnObj;
            columns.add(new Pair<String, Boolean>((String) columnJson
                    .get(RaceColumnMasterDataJsonSerializer.FIELD_NAME), (Boolean) columnJson
                    .get(RaceColumnMasterDataJsonSerializer.FIELD_MEDAL_RACE)));
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

    private ScoringScheme deserializeScoringScheme(JSONObject jsonObject) {
        String type = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_TYPE);
        return domainFactory.createScoringScheme(ScoringSchemeType.valueOf(type));
    }

    private int[] deserializeResultDesicardingRule(JSONObject jsonObject) {
        JSONArray indeces = (JSONArray) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_INDICES);
        if (indeces == null) {
            return null;
        }
        int[] result = new int[indeces.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Integer) indeces.get(i);
        }
        return result;
    }
}
