package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class RaceLogFinishPositioningEventSerializer extends BaseRaceLogEventSerializer {
    public static final String FIELD_POSITIONED_COMPETITORS = "positionedCompetitors";
    public static final String FIELD_COMPETITOR_ID = "competitorId";
    public static final String FIELD_COMPETITOR_NAME = "competitorName";
    public static final String FIELD_COMPETITOR_SHORT_NAME = "competitorShortName";
    public static final String FIELD_COMPETITOR_BOAT_NAME = "competitorBoatName";
    public static final String FIELD_COMPETITOR_BOAT_SAIL_ID = "competitorBoatSailId";
    public static final String FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON = "maxPointsReason";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_MERGE_STATE = "mergeState";
    public static final String FIELD_RANK = "rank";
    public static final String FIELD_FINISHING_TIME_POINT_AS_MILLIS = "finishingTimePointAsMillis";
    
    public RaceLogFinishPositioningEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected abstract String getClassFieldValue();

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogFinishPositioningEvent finishPositioningConfirmedEvent = (RaceLogFinishPositioningEvent) object;
        JSONObject result = super.serialize(finishPositioningConfirmedEvent);
        result.put(FIELD_POSITIONED_COMPETITORS, serializePositionedCompetitors(finishPositioningConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()));
        return result;
    }
    
    private JSONArray serializePositionedCompetitors(CompetitorResults positionedCompetitors) {
        JSONArray jsonPositionedCompetitors = new JSONArray();
        if (positionedCompetitors != null) { // for backwards compatibility reasons
            for (CompetitorResult positionedCompetitor : positionedCompetitors) {
                JSONObject jsonPositionedCompetitor = new JSONObject();
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_ID, positionedCompetitor.getCompetitorId().toString());
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_NAME, positionedCompetitor.getName());
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_SHORT_NAME, positionedCompetitor.getShortName());
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_BOAT_NAME, positionedCompetitor.getBoatName());
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_BOAT_SAIL_ID, positionedCompetitor.getBoatSailId());
                jsonPositionedCompetitor.put(FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON, positionedCompetitor.getMaxPointsReason().name());
                jsonPositionedCompetitor.put(FIELD_SCORE, positionedCompetitor.getScore());
                jsonPositionedCompetitor.put(FIELD_COMMENT, positionedCompetitor.getComment());
                jsonPositionedCompetitor.put(FIELD_MERGE_STATE, positionedCompetitor.getMergeState().name());
                jsonPositionedCompetitor.put(FIELD_RANK, positionedCompetitor.getOneBasedRank()); // writes it as an int, but deserializer will fetch it as a Long
                jsonPositionedCompetitor.put(FIELD_FINISHING_TIME_POINT_AS_MILLIS, positionedCompetitor.getFinishingTime() == null ? null : positionedCompetitor.getFinishingTime().asMillis());
                jsonPositionedCompetitors.add(jsonPositionedCompetitor);
            }
        }
        return jsonPositionedCompetitors;
    }

}
