package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPathfinderEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPathfinderEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogPathFinderEventDeserializer extends BaseRaceLogEventDeserializer implements
        JsonDeserializer<RaceLogEvent> {

    public RaceLogPathFinderEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint,
            int passId, List<Competitor> competitors) throws JsonDeserializationException {
        String pathfinderId = object.get(RaceLogPathfinderEventSerializer.FIELD_PATHFINDER_ID).toString();
        return new RaceLogPathfinderEventImpl(createdAt, timePoint, author, id, passId, pathfinderId);
    }

}
