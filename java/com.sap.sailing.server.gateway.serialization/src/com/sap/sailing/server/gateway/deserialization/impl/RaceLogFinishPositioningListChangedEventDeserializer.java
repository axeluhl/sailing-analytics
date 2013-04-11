package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningListChangedEventSerializer;

public class RaceLogFinishPositioningListChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogFinishPositioningListChangedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        
        JSONArray jsonPositionedCompetitors = Helpers.getNestedArraySafe(object, RaceLogFinishPositioningListChangedEventSerializer.FIELD_POSITIONED_COMPETITORS);
        List<Pair<Competitor, MaxPointsReason>> positionedCompetitors = deserializePositionedCompetitors(jsonPositionedCompetitors);

        return factory.createFinishPositioningListChangedEvent(timePoint, id, competitors, passId, positionedCompetitors);
    }

    private List<Pair<Competitor, MaxPointsReason>> deserializePositionedCompetitors(JSONArray jsonPositionedCompetitors) throws JsonDeserializationException {
        List<Pair<Competitor, MaxPointsReason>> positionedCompetitors = new ArrayList<Pair<Competitor,MaxPointsReason>>();
        
        for (Object object : jsonPositionedCompetitors) {
            JSONObject jsonPositionedCompetitor = Helpers.toJSONObjectSafe(object);
            
            JSONObject jsonCompetitor = Helpers.toJSONObjectSafe(jsonPositionedCompetitor.get(RaceLogFinishPositioningListChangedEventSerializer.FIELD_COMPETITOR));
            Competitor competitor = competitorDeserializer.deserialize(jsonCompetitor);
            
            String maxPointsReasonName = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningListChangedEventSerializer.FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON);
            MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName);
            
            Pair<Competitor, MaxPointsReason> positionedCompetitor = new Pair<Competitor, MaxPointsReason>(competitor, maxPointsReason);
            positionedCompetitors.add(positionedCompetitor);
        }
        
        return positionedCompetitors;
    }

}
