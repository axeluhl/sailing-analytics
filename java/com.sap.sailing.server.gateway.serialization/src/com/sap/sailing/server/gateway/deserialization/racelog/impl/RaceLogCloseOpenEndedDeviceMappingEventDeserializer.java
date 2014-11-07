package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class RaceLogCloseOpenEndedDeviceMappingEventDeserializer extends BaseRaceLogEventDeserializer {
    public static final String FIELD_DEVICE_MAPPING_EVENT_ID = "deviceMappingEventId";
    public static final String FIELD_CLOSING_TIMEPOINT_MILLIS = "closingTimePointMillis";
    
    public RaceLogCloseOpenEndedDeviceMappingEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
    	Serializable deviceMappingEventId = Helpers.tryUuidConversion((Serializable) object.get(FIELD_DEVICE_MAPPING_EVENT_ID));
    	TimePoint closingTimePoint = new MillisecondsTimePoint((Long) object.get(FIELD_CLOSING_TIMEPOINT_MILLIS));
        
        return factory.createCloseOpenEndedDeviceMappingEvent(createdAt, author, timePoint, id, passId,
                deviceMappingEventId, closingTimePoint);
    }
}
