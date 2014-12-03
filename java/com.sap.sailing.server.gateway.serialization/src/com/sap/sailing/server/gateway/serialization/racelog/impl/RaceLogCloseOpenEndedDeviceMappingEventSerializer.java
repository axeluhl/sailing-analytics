package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogCloseOpenEndedDeviceMappingEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogCloseOpenEndedDeviceMappingEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = CloseOpenEndedDeviceMappingEvent.class.getSimpleName();

    public RaceLogCloseOpenEndedDeviceMappingEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        CloseOpenEndedDeviceMappingEvent event = (CloseOpenEndedDeviceMappingEvent) object;

        JSONObject result = super.serialize(event);
        result.put(RaceLogCloseOpenEndedDeviceMappingEventDeserializer.FIELD_DEVICE_MAPPING_EVENT_ID, event.getDeviceMappingEventId());
        result.put(RaceLogCloseOpenEndedDeviceMappingEventDeserializer.FIELD_CLOSING_TIMEPOINT_MILLIS, event.getClosingTimePoint().asMillis());
        
        return result;
    }

}
