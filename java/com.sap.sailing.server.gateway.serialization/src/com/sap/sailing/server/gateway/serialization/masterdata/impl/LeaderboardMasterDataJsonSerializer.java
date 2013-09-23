package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardMasterDataJsonSerializer implements JsonSerializer<Leaderboard> {

    public static final String FIELD_FOR_RACE_COLUMNS = "forRaceColumns";
    public static final String FIELD_TIME_POINT = "timePoint";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_MAX_POINTS_REASON = "maxPointsReason";
    public static final String FIELD_EXPLICIT_SCORE_CORRECTION = "explicitScoreCorrection";
    public static final String FIELD_FOR_COMPETITORS = "forCompetitors";
    public static final String FIELD_RACE_COLUMN_NAME = "raceColumnName";
    public static final String FIELD_RACE_COLUMNS = "raceColumns";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SCORE_CORRECTION = "scoreCorrection";
    public static final String FIELD_SCORING_SCHEME = "scoringScheme";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_REGATTA_LEADERBOARD = "regattaLeaderboard";
    public static final String FIELD_META_LEADERBOARD = "metaLeaderboard";
    public static final String FIELD_RESULT_DISCARDING_RULE = "resultDiscardingRule";
    public static final String FIELD_INDICES = "indices";
    public static final String FIELD_COURSE_AREA = "courseArea";
    public static final String FIELD_ID = "id";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_COMPETITOR_ID = "competitorId";
    public static final String FIELD_CARRIED_POINTS = "carriedPoints";
    public static final String FIELD_CARRIED = "carried";
    public static final String FIELD_SUPPRESSED_COMPETITORS = "suppressedCompetitors";
    public static final String FIELD_COMPETITOR_DISPLAY_NAMES = "competitorDisplayNames";
    public static final String FIELD_RACE_LOG_EVENTS = "raceLogEvents";
    public static final String FIELD_FLEETS = "fleets";
    public static final String FIELD_FLEET_NAME = "fleetName";

    private final JsonSerializer<Competitor> competitorSerializer;
    private final JsonSerializer<RaceColumn> raceColumnSerializer;
    private final JsonSerializer<RaceLogEvent> raceLogEventSerializer;

    /*
     * TODO This is a hack to "remember" the course areas to allow finding the events which own the course areas. This
     * way regatta leaderboard can be completely exported, because all events, their regattas, etc can be exported too.
     * Their should be a proper connection between regatta and event soon.
     */
    private final Set<String> courseAreaIds = new HashSet<String>();

    private final Set<Regatta> regattas = new HashSet<Regatta>();

    public LeaderboardMasterDataJsonSerializer(JsonSerializer<Competitor> competitorSerializer,
            JsonSerializer<RaceColumn> raceColumnSerializer, JsonSerializer<RaceLogEvent> raceLogEventSerializer) {
        this.competitorSerializer = competitorSerializer;
        this.raceColumnSerializer = raceColumnSerializer;
        this.raceLogEventSerializer = raceLogEventSerializer;
    }

    @Override
    public JSONObject serialize(Leaderboard leaderboard) {
        if (leaderboard == null) {
            return null;
        }
        JSONObject jsonLeaderboard = new JSONObject();
        jsonLeaderboard.put(FIELD_NAME, leaderboard.getName());
        jsonLeaderboard.put(FIELD_SCORE_CORRECTION, createJsonForScoreCorrection(leaderboard));
        jsonLeaderboard.put(FIELD_CARRIED_POINTS, createJsonArrayForCarriedPoints(leaderboard));
        jsonLeaderboard.put(FIELD_SUPPRESSED_COMPETITORS,
                createJsonArrayForSuppressedCompetitors(leaderboard.getSuppressedCompetitors()));
        jsonLeaderboard.put(FIELD_COMPETITOR_DISPLAY_NAMES, createJsonArrayForCompetitorDisplayNames(leaderboard));
        jsonLeaderboard.put(FIELD_COMPETITORS, createJsonArrayForCompetitors(leaderboard.getAllCompetitors()));
        jsonLeaderboard.put(FIELD_RESULT_DISCARDING_RULE,
                createJsonForResultDiscardingRule(leaderboard.getResultDiscardingRule()));
        jsonLeaderboard.put(FIELD_DISPLAY_NAME, leaderboard.getDisplayName());
        jsonLeaderboard.put(FIELD_RACE_LOG_EVENTS, createJsonArrayForRaceLogEventsPerRaceColumn(leaderboard));
        boolean isRegattaLeaderboard = false;
        if (leaderboard instanceof FlexibleLeaderboard) {
            FlexibleLeaderboard flexibleLeaderboard = (FlexibleLeaderboard) leaderboard;
            jsonLeaderboard.put(FIELD_SCORING_SCHEME, createJsonForScoringScheme(leaderboard.getScoringScheme()));
            jsonLeaderboard.put(FIELD_COURSE_AREA, createJsonForCourseArea(flexibleLeaderboard.getDefaultCourseArea()));
            jsonLeaderboard.put(FIELD_RACE_COLUMNS, createJsonArrayForRaceColumns(leaderboard.getRaceColumns()));
        } else if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            jsonLeaderboard.put(FIELD_REGATTA_NAME, regattaLeaderboard.getRegatta().getName());
            CourseArea courseArea = regattaLeaderboard.getRegatta().getDefaultCourseArea();
            if (courseArea != null) {
                courseAreaIds.add(courseArea.getId().toString());
            }
            regattas.add(regattaLeaderboard.getRegatta());
            isRegattaLeaderboard = true;
        }
        jsonLeaderboard.put(FIELD_REGATTA_LEADERBOARD, isRegattaLeaderboard);
        return jsonLeaderboard;
    }

    private JSONArray createJsonArrayForRaceLogEventsPerRaceColumn(Leaderboard leaderboard) {
        JSONArray array = new JSONArray();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            JSONObject raceColumnRaceLogJson = new JSONObject();
            raceColumnRaceLogJson.put(FIELD_RACE_COLUMN_NAME, raceColumn.getName());
            raceColumnRaceLogJson.put(FIELD_FLEETS, createJsonArrayForRaceLogEventsForFleets(raceColumn));
            array.add(raceColumnRaceLogJson);
        }
        return array;
    }

    private JSONArray createJsonArrayForRaceLogEventsForFleets(RaceColumn raceColumn) {
        JSONArray array = new JSONArray();
        for (Fleet fleet : raceColumn.getFleets()) {
            JSONObject fleetRaceLogJson = new JSONObject();
            fleetRaceLogJson.put(FIELD_FLEET_NAME, fleet.getName());
            fleetRaceLogJson.put(FIELD_RACE_LOG_EVENTS, createRaceLogEventsForFleet(raceColumn, fleet));
            array.add(fleetRaceLogJson);
        }
        return array;
    }

    private JSONArray createRaceLogEventsForFleet(RaceColumn raceColumn, Fleet fleet) {
        JSONArray array = new JSONArray();
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null) {
            raceLog.lockForRead();
            for (RaceLogEvent event : raceLog.getFixes()) {
                array.add(raceLogEventSerializer.serialize(event));
            }
            raceLog.unlockAfterRead();
        }
        return array;
    }

    private JSONArray createJsonArrayForCompetitorDisplayNames(Leaderboard leaderboard) {
        JSONArray array = new JSONArray();
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            String displayName = leaderboard.getDisplayName(competitor);
            if (displayName != null) {
                JSONObject displayNameJson = new JSONObject();
                displayNameJson.put(FIELD_COMPETITOR_ID, competitor.getId().toString());
                displayNameJson.put(FIELD_DISPLAY_NAME, displayName);
                array.add(displayNameJson);
            }
        }
        return array;
    }

    private JSONArray createJsonArrayForSuppressedCompetitors(Iterable<Competitor> suppressedCompetitors) {
        JSONArray array = new JSONArray();
        for (Competitor competitor : suppressedCompetitors) {
            array.add(competitor.getId().toString());
        }
        return array;
    }

    private JSONArray createJsonArrayForCarriedPoints(Leaderboard leaderboard) {
        JSONArray jsonArray = new JSONArray();
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            double carriedPoints = leaderboard.getCarriedPoints(competitor);
            if (carriedPoints != 0) {
                jsonArray.add(createJsonForCarriedPoints(competitor.getId().toString(), carriedPoints));
            }
        }

        return jsonArray;
    }

    private JSONObject createJsonForCarriedPoints(String id, double carriedPoints) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FIELD_COMPETITOR_ID, id);
        jsonObject.put(FIELD_CARRIED, carriedPoints);
        return jsonObject;
    }

    private JSONObject createJsonForCourseArea(CourseArea defaultCourseArea) {
        if (defaultCourseArea == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, defaultCourseArea.getId());
        result.put(FIELD_NAME, defaultCourseArea.getName());
        return null;
    }

    public static JSONObject createJsonForResultDiscardingRule(ResultDiscardingRule resultDiscardingRule) {
        JSONObject result = new JSONObject();
        if (resultDiscardingRule instanceof ThresholdBasedResultDiscardingRule) {
            ThresholdBasedResultDiscardingRule rule = (ThresholdBasedResultDiscardingRule) resultDiscardingRule;
            JSONArray indices = new JSONArray();
            int[] rawValues = rule.getDiscardIndexResultsStartingWithHowManyRaces();
            for (int i = 0; i < rawValues.length; i++) {
                indices.add(new Integer(rawValues[i]));
            }
            result.put(FIELD_INDICES, indices);
        }
        return result;
    }

    public static JSONObject createJsonForScoringScheme(ScoringScheme scoringScheme) {
        JSONObject result = new JSONObject();
        result.put(FIELD_TYPE, scoringScheme.getType().name());
        return result;
    }

    private JSONArray createJsonArrayForRaceColumns(Iterable<RaceColumn> raceColumns) {
        JSONArray jsonRaceColumns = new JSONArray();
        for (RaceColumn raceColumn : raceColumns) {
            JSONObject jsonRaceColumn = raceColumnSerializer.serialize(raceColumn);
            jsonRaceColumns.add(jsonRaceColumn);
        }
        return jsonRaceColumns;
    }

    private JSONArray createJsonArrayForCompetitors(Iterable<Competitor> allCompetitors) {
        JSONArray jsonCompetitors = new JSONArray();
        for (Competitor competitor : allCompetitors) {
            JSONObject jsonCompetitor = competitorSerializer.serialize(competitor);
            jsonCompetitors.add(jsonCompetitor);
        }
        return jsonCompetitors;
    }

    private JSONObject createJsonForScoreCorrection(Leaderboard leaderboard) {
        SettableScoreCorrection correction = leaderboard.getScoreCorrection();
        JSONObject jsonScoreCorrection = new JSONObject();
        jsonScoreCorrection.put(FIELD_COMMENT, correction.getComment());
        if (correction.getTimePointOfLastCorrectionsValidity() != null) {
            jsonScoreCorrection.put(FIELD_TIME_POINT, correction.getTimePointOfLastCorrectionsValidity().asMillis());
        } else {
            jsonScoreCorrection.put(FIELD_TIME_POINT, null);
        }

        jsonScoreCorrection.put(FIELD_FOR_RACE_COLUMNS,
                createJSONArrayForScoreCorrectionsForRaceColumns(correction, leaderboard));

        return jsonScoreCorrection;
    }

    private JSONArray createJSONArrayForScoreCorrectionsForRaceColumns(SettableScoreCorrection correction,
            Leaderboard leaderboard) {
        JSONArray scoreCorrectionsForRaceColumns = new JSONArray();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (correction.hasCorrectionFor(raceColumn)) {
                JSONObject scoreCorrectionsForRaceColumn = new JSONObject();
                scoreCorrectionsForRaceColumn.put(FIELD_RACE_COLUMN_NAME, raceColumn.getName());
                scoreCorrectionsForRaceColumn.put(FIELD_FOR_COMPETITORS,
                        createJsonForScoreCorrectionsForRaceColumnAndCompetitors(correction, raceColumn, leaderboard));
                scoreCorrectionsForRaceColumns.add(scoreCorrectionsForRaceColumn);
            }

        }

        return scoreCorrectionsForRaceColumns;
    }

    private JSONArray createJsonForScoreCorrectionsForRaceColumnAndCompetitors(SettableScoreCorrection correction,
            RaceColumn raceColumn, Leaderboard leaderboard) {
        JSONArray scoreCorrectionsForCompetitors = new JSONArray();
        for (Competitor competitor : leaderboard.getAllCompetitors()) {
            // TODO bug 655: score corrections shall be time dependent
            if (correction.isScoreCorrected(competitor, raceColumn, MillisecondsTimePoint.now())) {
                JSONObject scoreCorrectionForCompetitor = new JSONObject();
                scoreCorrectionForCompetitor.put(FIELD_EXPLICIT_SCORE_CORRECTION,
                        correction.getExplicitScoreCorrection(competitor, raceColumn));
                scoreCorrectionForCompetitor.put(FIELD_MAX_POINTS_REASON,
                        correction.getMaxPointsReason(competitor, raceColumn).toString());
                scoreCorrectionForCompetitor.put(FIELD_COMPETITOR_ID, competitor.getId().toString());
                scoreCorrectionsForCompetitors.add(scoreCorrectionForCompetitor);
            }
        }

        return scoreCorrectionsForCompetitors;
    }

    public Set<String> getCourseAreaIds() {
        return courseAreaIds;
    }

    public Iterable<Regatta> getRegattas() {
        return regattas;
    }

}
