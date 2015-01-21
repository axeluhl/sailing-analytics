package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogAdditionalScoringInformationEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogAdditionalScoringInformationSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogAdditionalScoringInformationEvent.class.getSimpleName();

    public RaceLogAdditionalScoringInformationSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogAdditionalScoringInformationEvent event = (RaceLogAdditionalScoringInformationEvent) object;
        JSONObject result = super.serialize(event);
        result.put(RaceLogAdditionalScoringInformationEventDeserializer.FIELD_TYPE, event.getType().name());
        return result;
    }
}
