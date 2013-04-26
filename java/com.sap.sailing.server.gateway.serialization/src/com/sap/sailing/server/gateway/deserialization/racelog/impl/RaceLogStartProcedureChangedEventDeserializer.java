package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogStartProcedureChangedEventSerializer;

public class RaceLogStartProcedureChangedEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogStartProcedureChangedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, TimePoint timePoint,
            int passId, List<Competitor> competitors) throws JsonDeserializationException {
        StartProcedureType type = StartProcedureType.valueOf(object.get(RaceLogStartProcedureChangedEventSerializer.FIELD_START_PROCEDURE_TYPE).toString());
        return factory.createStartProcedureChangedEvent(createdAt, timePoint, id, competitors, passId, type);
    }

}
