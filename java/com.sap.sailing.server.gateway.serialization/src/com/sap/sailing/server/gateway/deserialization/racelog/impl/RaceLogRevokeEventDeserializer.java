package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRevokeEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRevokeEventDeserializer extends BaseRaceLogEventDeserializer {	
    public RaceLogRevokeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
    	Serializable revokedEventId = Helpers.tryUuidConversion(
    			(Serializable) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_ID));
        String revokedEventType = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_TYPE);
        String revokedEventShortInfo = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_SHORT_INFO);
        String reason = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REASON);
    	
        return factory.createRevokeEvent(createdAt, author, timePoint, id, passId, revokedEventId,
                revokedEventType, revokedEventShortInfo, reason);
    }
}
