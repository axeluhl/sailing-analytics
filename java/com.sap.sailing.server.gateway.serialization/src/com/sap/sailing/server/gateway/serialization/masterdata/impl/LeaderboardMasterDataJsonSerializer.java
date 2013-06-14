package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardMasterDataJsonSerializer implements JsonSerializer<Leaderboard> {
    
    public static final String FIELD_MAX_POINTS_REASON = "maxPointsReason";
    public static final String FIELD_EXPLICIT_SCORE_CORRECTION = "explicitScoreCorrection";
    public static final String FIELD_FOR_COMPETITORS = "forCompetitors";
    public static final String FIELD_RACE_COLUMN_NAME = "raceColumnName";
    public static final String FIELD_RACE_COLUMNS = "raceColumns";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SCORE_CORRECTION = "scoreCorrection";
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

        return jsonLeaderboard;
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
        jsonScoreCorrection.put("comment", correction.getComment());
        jsonScoreCorrection.put("timePoint", correction.getTimePointOfLastCorrectionsValidity());
        jsonScoreCorrection.put("forRaceColumns",
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
