package com.sap.sailing.server.gateway.serialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util;

public class RaceLogFinishPositioningConfirmedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningConfirmedEvent.class.getSimpleName();
    
    public static final String FIELD_POSITIONED_COMPETITORS = "positionedCompetitors";
    public static final String FIELD_COMPETITOR_ID = "competitorId";
    public static final String FIELD_COMPETITOR_NAME = "competitorName";
    public static final String FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON = "maxPointsReason";
    
    public RaceLogFinishPositioningConfirmedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogFinishPositioningConfirmedEvent finishPositioningConfirmedEvent = (RaceLogFinishPositioningConfirmedEvent) object;

        JSONObject result = super.serialize(finishPositioningConfirmedEvent);
        
        result.put(FIELD_POSITIONED_COMPETITORS, serializePositionedCompetitors(finishPositioningConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()));

        return result;
    }
    
    private JSONArray serializePositionedCompetitors(List<Util.Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        JSONArray jsonPositionedCompetitors = new JSONArray();
        
        if (positionedCompetitors != null) { // for backwards compatibility reasons
            for (Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor : positionedCompetitors) {
                JSONObject jsonPositionedCompetitor = new JSONObject();

                jsonPositionedCompetitor.put(FIELD_COMPETITOR_ID, positionedCompetitor.getA().toString());
                jsonPositionedCompetitor.put(FIELD_COMPETITOR_NAME, positionedCompetitor.getB());
                jsonPositionedCompetitor.put(FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON, positionedCompetitor.getC().name());
                jsonPositionedCompetitors.add(jsonPositionedCompetitor);
            }
        }
        
        return jsonPositionedCompetitors;
    }

}
