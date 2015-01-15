package com.sap.sailing.server.gateway.serialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util;

public class RaceLogFinishPositioningListChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningListChangedEvent.class.getSimpleName();
    public static final String FIELD_POSITIONED_COMPETITORS = "positionedCompetitors";
    public static final String FIELD_COMPETITOR_ID = "competitorId";
    public static final String FIELD_COMPETITOR_NAME = "competitorName";
    public static final String FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON = "maxPointsReason";
    
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

    private JSONArray serializePositionedCompetitors(List<Util.Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        JSONArray jsonPositionedCompetitors = new JSONArray();
        
        for (Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor : positionedCompetitors) {
            JSONObject jsonPositionedCompetitor = new JSONObject();
            
            jsonPositionedCompetitor.put(FIELD_COMPETITOR_ID, positionedCompetitor.getA().toString());
            jsonPositionedCompetitor.put(FIELD_COMPETITOR_NAME, positionedCompetitor.getB());
            jsonPositionedCompetitor.put(FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON, positionedCompetitor.getC().name());
            jsonPositionedCompetitors.add(jsonPositionedCompetitor);
        }
        
        return jsonPositionedCompetitors;
    }

}
