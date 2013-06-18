package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
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
    public static final String FIELD_RESULT_DISCARDING_RULE = "resultDiscardingRule";
    public static final String FIELD_INDICES = "indices";
    public static final String FIELD_COURSE_AREA = "courseArea";
    public static final String FIELD_ID = "id";
    private final JsonSerializer<Competitor> competitorSerializer;
    private final JsonSerializer<RaceColumn> raceColumnSerializer;
    
    

    public LeaderboardMasterDataJsonSerializer(JsonSerializer<Competitor> competitorSerializer,
            JsonSerializer<RaceColumn> raceColumnSerializer) {
        this.competitorSerializer = competitorSerializer;
        this.raceColumnSerializer = raceColumnSerializer;
    }

    @Override
    public JSONObject serialize(Leaderboard leaderboard) {
        if (leaderboard == null) {
            return null;
        }
        JSONObject jsonLeaderboard = new JSONObject();
        jsonLeaderboard.put(FIELD_NAME, leaderboard.getName());
        jsonLeaderboard.put(FIELD_SCORE_CORRECTION, createJsonForScoreCorrection(leaderboard));
        jsonLeaderboard.put(FIELD_COMPETITORS, createJsonArrayForCompetitors(leaderboard.getAllCompetitors()));
        jsonLeaderboard.put(FIELD_RACE_COLUMNS, createJsonArrayForRaceColumns(leaderboard.getRaceColumns()));
        jsonLeaderboard.put(FIELD_RESULT_DISCARDING_RULE, createJsonForResultDiscardingRule(leaderboard.getResultDiscardingRule()));
        boolean isRegattaLeaderboard = false;
        if (leaderboard instanceof FlexibleLeaderboard) {
            FlexibleLeaderboard flexibleLeaderboard = (FlexibleLeaderboard) leaderboard;
            jsonLeaderboard.put(FIELD_SCORING_SCHEME, createJsonForScoringScheme(leaderboard.getScoringScheme()));
            jsonLeaderboard.put(FIELD_COURSE_AREA, createJsonForCourseArea(flexibleLeaderboard.getDefaultCourseArea()));
        } else {
            isRegattaLeaderboard = true;
        }
        
        jsonLeaderboard.put(FIELD_REGATTA_LEADERBOARD, isRegattaLeaderboard);

        return jsonLeaderboard;
    }
    
    private JSONObject createJsonForCourseArea(CourseArea defaultCourseArea) {
        if (defaultCourseArea == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, defaultCourseArea.getId());
        result.put(FIELD_NAME, defaultCourseArea.getName());
        // TODO Auto-generated method stub
        return null;
    }

    private JSONObject createJsonForResultDiscardingRule(ResultDiscardingRule resultDiscardingRule) {
        JSONObject result = new JSONObject();
        if (resultDiscardingRule instanceof ThresholdBasedResultDiscardingRule) {
            ThresholdBasedResultDiscardingRule rule = (ThresholdBasedResultDiscardingRule) resultDiscardingRule;
            JSONArray indices = new JSONArray();
            int[] rawValues = rule.getDiscardIndexResultsStartingWithHowManyRaces();
            for (int i = 0; i < rawValues.length; i++) {
                indices.add(rawValues[i]);
            }
            result.put(FIELD_INDICES, indices);
        }
        return result;
    }

    private JSONObject createJsonForScoringScheme(ScoringScheme scoringScheme) {
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
        jsonScoreCorrection.put(FIELD_TIME_POINT, correction.getTimePointOfLastCorrectionsValidity());
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
            JSONObject scoreCorrectionForCompetitor = new JSONObject();
            scoreCorrectionForCompetitor.put(FIELD_EXPLICIT_SCORE_CORRECTION,
                    correction.getExplicitScoreCorrection(competitor, raceColumn));
            scoreCorrectionForCompetitor.put(FIELD_MAX_POINTS_REASON,
                    correction.getMaxPointsReason(competitor, raceColumn));
            scoreCorrectionsForCompetitors.add(scoreCorrectionForCompetitor);
        }

        return scoreCorrectionsForCompetitors;
    }

}
