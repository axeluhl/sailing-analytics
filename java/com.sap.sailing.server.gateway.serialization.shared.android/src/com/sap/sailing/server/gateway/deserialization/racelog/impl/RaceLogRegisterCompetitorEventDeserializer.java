package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRegisterCompetitorEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorEventDeserializer extends BaseRaceLogEventDeserializer {
    private final BoatJsonDeserializer boatDeserializer;

    public RaceLogRegisterCompetitorEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer, 
            BoatJsonDeserializer boatDeserializer) {
        super(competitorDeserializer);
        this.boatDeserializer = boatDeserializer;
    }
 
    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        assert competitors.size() == 1 : "Expected exactly one competitor for RegisterCompetitorEvent";
        JSONObject competitorObject = (JSONObject) (object
                .get(RaceLogRegisterCompetitorEventSerializer.FIELD_COMPETITOR_WITHBOAT) != null
                        ? object.get(RaceLogRegisterCompetitorEventSerializer.FIELD_COMPETITOR_WITHBOAT)
                        : object.get(RaceLogRegisterCompetitorEventSerializer.FIELD_COMPETITOR));
        JSONObject boatObject = (JSONObject) object.get(RaceLogRegisterCompetitorEventSerializer.FIELD_BOAT);
        Competitor competitor = competitorDeserializer.deserialize(competitorObject);
        Boat boat = boatDeserializer.deserialize(boatObject);
        return new RaceLogRegisterCompetitorEventImpl(createdAt, timePoint, author, id, passId, competitor, boat);
    }
}
