package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRevokeEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RevokeEvent.class.getSimpleName();
    public static final String FIELD_REVOKED_EVENT_ID = "revokedEventId";

    public RaceLogRevokeEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RevokeEvent event = (RevokeEvent) object;

        JSONObject result = super.serialize(event);
        result.put(FIELD_REVOKED_EVENT_ID, event.getRevokedEventId().toString());

        return result;
    }

}
