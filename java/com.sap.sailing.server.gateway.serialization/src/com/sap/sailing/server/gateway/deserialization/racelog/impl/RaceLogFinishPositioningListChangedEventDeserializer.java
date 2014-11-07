package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningListChangedEventSerializer;
import com.sap.sse.common.Util;

public class RaceLogFinishPositioningListChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogFinishPositioningListChangedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        
        JSONArray jsonPositionedCompetitors = Helpers.getNestedArraySafe(object, RaceLogFinishPositioningListChangedEventSerializer.FIELD_POSITIONED_COMPETITORS);
        CompetitorResults positionedCompetitors = deserializePositionedCompetitors(jsonPositionedCompetitors);

        return factory.createFinishPositioningListChangedEvent(createdAt, author, timePoint, id, competitors, passId, positionedCompetitors);
    }

    private CompetitorResults deserializePositionedCompetitors(JSONArray jsonPositionedCompetitors) throws JsonDeserializationException {
        CompetitorResults positionedCompetitors = new CompetitorResultsImpl();
        
        for (Object object : jsonPositionedCompetitors) {
            JSONObject jsonPositionedCompetitor = Helpers.toJSONObjectSafe(object);
            
            Serializable competitorId = (Serializable) jsonPositionedCompetitor.get(RaceLogFinishPositioningListChangedEventSerializer.FIELD_COMPETITOR_ID);
            competitorId = Helpers.tryUuidConversion(competitorId);
            String competitorName = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningListChangedEventSerializer.FIELD_COMPETITOR_NAME);
            
            String maxPointsReasonName = (String) jsonPositionedCompetitor.get(RaceLogFinishPositioningListChangedEventSerializer.FIELD_SCORE_CORRECTIONS_MAX_POINTS_REASON);
            MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName);
            
            Util.Triple<Serializable, String, MaxPointsReason> positionedCompetitor = new Util.Triple<Serializable, String, MaxPointsReason>(competitorId, competitorName, maxPointsReason);
            positionedCompetitors.add(positionedCompetitor);
        }
        
        return positionedCompetitors;
    }

}
