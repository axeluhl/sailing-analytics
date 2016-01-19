package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFinishPositioningListChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningListChangedEvent.class.getSimpleName();
    public static final String FIELD_POSITIONED_COMPETITORS = "positionedCompetitors";
    public static final String FIELD_COMPETITOR_ID = "competitorId";
    public static final String FIELD_COMPETITOR_NAME = "competitorName";
    public static final String FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON = "maxPointsReason";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_RANK = "rank";
    public static final String FIELD_FINISHING_TIME_POINT_AS_MILLIS = "finishingTimePointAsMillis";
    
    public RaceLogFinishPositioningListChangedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogFinishPositioningListChangedEvent finishPositioningListEvent = (RaceLogFinishPositioningListChangedEvent) object;
        JSONObject result = super.serialize(finishPositioningListEvent);
        result.put(FIELD_POSITIONED_COMPETITORS, serializePositionedCompetitors(finishPositioningListEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()));
        return result;
    }

    private JSONArray serializePositionedCompetitors(CompetitorResults positionedCompetitors) {
        JSONArray jsonPositionedCompetitors = new JSONArray();
        for (CompetitorResult positionedCompetitor : positionedCompetitors) {
            JSONObject jsonPositionedCompetitor = new JSONObject();
            jsonPositionedCompetitor.put(FIELD_COMPETITOR_ID, positionedCompetitor.getCompetitorId().toString());
            jsonPositionedCompetitor.put(FIELD_COMPETITOR_NAME, positionedCompetitor.getCompetitorDisplayName());
            jsonPositionedCompetitor.put(FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON, positionedCompetitor.getMaxPointsReason().name());
            jsonPositionedCompetitor.put(FIELD_SCORE, positionedCompetitor.getScore());
            jsonPositionedCompetitor.put(FIELD_COMMENT, positionedCompetitor.getComment());
            jsonPositionedCompetitor.put(FIELD_RANK, positionedCompetitor.getOneBasedRank());
            jsonPositionedCompetitor.put(FIELD_FINISHING_TIME_POINT_AS_MILLIS, positionedCompetitor.getFinishingTime() == null ? null : positionedCompetitor.getFinishingTime().asMillis());
            jsonPositionedCompetitors.add(jsonPositionedCompetitor);
        }
        
        return jsonPositionedCompetitors;
    }

}
