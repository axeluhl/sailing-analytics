package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogAdditionalScoringInformationEventDeserializer extends BaseRaceLogEventDeserializer {
    public static final String FIELD_TYPE = "additionalScoringInformationType";
    
    public RaceLogAdditionalScoringInformationEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        AdditionalScoringInformationType informationType = AdditionalScoringInformationType.valueOf((String)object.get(FIELD_TYPE));
        return factory.createAdditionalScoringInformationEvent(timePoint, id, author, passId, informationType);
    }
}
