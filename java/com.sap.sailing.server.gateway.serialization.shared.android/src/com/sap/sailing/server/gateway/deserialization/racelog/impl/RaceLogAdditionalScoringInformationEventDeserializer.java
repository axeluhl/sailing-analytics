package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.impl.RaceLogAdditionalScoringInformationEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogAdditionalScoringInformationEventDeserializer extends BaseRaceLogEventDeserializer {
    public static final String FIELD_TYPE = "additionalScoringInformationType";
    
    public RaceLogAdditionalScoringInformationEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        AdditionalScoringInformationType informationType = AdditionalScoringInformationType.valueOf((String)object.get(FIELD_TYPE));
        return new RaceLogAdditionalScoringInformationEventImpl(createdAt, timePoint, author, id, passId, informationType);
    }
}
