package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;

public class RaceLogRaceStatusEventDeserializer extends BaseRaceLogEventDeserializer {

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException {

        String statusValue = object.get(RaceLogRaceStatusEventSerializer.FIELD_NEXT_STATUS).toString();
        RaceLogRaceStatus nextStatus = RaceLogRaceStatus.valueOf(statusValue);

        return new RaceLogRaceStatusEventImpl(timePoint, id, Collections.<Competitor> emptyList(), passId, nextStatus);
    }

}
