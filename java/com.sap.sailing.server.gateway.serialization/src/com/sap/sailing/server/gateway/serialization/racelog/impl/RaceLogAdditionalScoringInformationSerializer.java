package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogAdditionalScoringInformationSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = AdditionalScoringInformationEvent.class.getSimpleName();

    public RaceLogAdditionalScoringInformationSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        AdditionalScoringInformationEvent event = (AdditionalScoringInformationEvent) object;

        JSONObject result = super.serialize(event);
        //TODO
        //result.put(RaceLogCloseOpenEndedDeviceMappingEventDeserializer.FIELD_DEVICE_MAPPING_EVENT_ID, event.getDeviceMappingEventId());
        //result.put(RaceLogCloseOpenEndedDeviceMappingEventDeserializer.FIELD_CLOSING_TIMEPOINT_MILLIS, event.getClosingTimePoint().asMillis());
        
        return result;
    }

}
