package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.RevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRevokeEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RevokeEvent.class.getSimpleName();
    public static final String FIELD_REVOKED_EVENT_ID = "revokedEventId";
    public static final String FIELD_REVOKED_EVENT_TYPE = "revokedEventType";
    public static final String FIELD_REVOKED_EVENT_SHORT_INFO = "revokedEventShortInfo";
    public static final String FIELD_REASON = "reason";

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
        RaceLogRevokeEvent event = (RaceLogRevokeEvent) object;
        JSONObject result = super.serialize(event);
        result.put(FIELD_REVOKED_EVENT_ID, event.getRevokedEventId().toString());
        result.put(FIELD_REVOKED_EVENT_TYPE, event.getRevokedEventType());
        result.put(FIELD_REVOKED_EVENT_SHORT_INFO, event.getRevokedEventShortInfo());
        result.put(FIELD_REASON, event.getReason());

        return result;
    }

}
