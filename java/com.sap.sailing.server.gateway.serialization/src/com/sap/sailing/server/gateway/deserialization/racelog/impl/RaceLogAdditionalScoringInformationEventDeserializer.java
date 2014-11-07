package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

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
