package com.sap.sailing.server.gateway.serialization.racelog.impl;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFinishPositioningListChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningListChangedEvent.class.getSimpleName();
    public static final String FIELD_POSITIONED_COMPETITORS = "positionedCompetitors";
    public static final String FIELD_COMPETITOR = "competitor";
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
        
        result.put(FIELD_POSITIONED_COMPETITORS, serializePositionedCompetitors(finishPositioningListEvent.getPositionedCompetitors()));

        return result;
    }

    private JSONArray serializePositionedCompetitors(List<Pair<Competitor, MaxPointsReason>> positionedCompetitors) {
        JSONArray jsonPositionedCompetitors = new JSONArray();
        
        for (Pair<Competitor, MaxPointsReason> positionedCompetitor : positionedCompetitors) {
            JSONObject jsonPositionedCompetitor = new JSONObject();
            if (positionedCompetitor == null || positionedCompetitor.getA() == null) {
                continue;
            }
            jsonPositionedCompetitor.put(FIELD_COMPETITOR, competitorSerializer.serialize(positionedCompetitor.getA()));
            jsonPositionedCompetitor.put(FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON, positionedCompetitor.getB().name());
            jsonPositionedCompetitors.add(jsonPositionedCompetitor);
        }
        
        return jsonPositionedCompetitors;
    }

}
