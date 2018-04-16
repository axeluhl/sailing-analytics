package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRaceStatusEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogRaceStatusEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        String statusValue = object.get(RaceLogRaceStatusEventSerializer.FIELD_NEXT_STATUS).toString();
        RaceLogRaceStatus nextStatus = RaceLogRaceStatus.valueOf(statusValue);
        return new RaceLogRaceStatusEventImpl(createdAt, timePoint, author, id, passId, nextStatus);
    }
}
