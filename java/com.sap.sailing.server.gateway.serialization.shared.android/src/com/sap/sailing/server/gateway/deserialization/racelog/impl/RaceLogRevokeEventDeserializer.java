package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRevokeEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRevokeEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.impl.UUIDHelper;

public class RaceLogRevokeEventDeserializer extends BaseRaceLogEventDeserializer {	
    public RaceLogRevokeEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
    	Serializable revokedEventId = UUIDHelper.tryUuidConversion(
    			(Serializable) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_ID));
        String revokedEventType = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_TYPE);
        String revokedEventShortInfo = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REVOKED_EVENT_SHORT_INFO);
        String reason = (String) object.get(RaceLogRevokeEventSerializer.FIELD_REASON);
    	
        return new RaceLogRevokeEventImpl(createdAt, timePoint, author, id, passId, revokedEventId,
                revokedEventType, revokedEventShortInfo, reason);
    }
}
