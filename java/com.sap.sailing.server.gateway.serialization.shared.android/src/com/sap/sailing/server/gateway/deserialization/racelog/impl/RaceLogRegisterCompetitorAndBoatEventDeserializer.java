package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorAndBoatEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatJsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorAndBoatEventDeserializer extends BaseRaceLogEventDeserializer {
    private BoatJsonDeserializer boatDeserializer;
    
    public RaceLogRegisterCompetitorAndBoatEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer, BoatJsonDeserializer boatDeserializer) {
        super(competitorDeserializer);
        this.boatDeserializer = boatDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        assert competitors.size() == 1 : "Expected exactly one competitor for RegisterCompetitorEvent";
        Boat boat = boatDeserializer.deserialize(object);
    	return new RaceLogRegisterCompetitorAndBoatEventImpl(createdAt, timePoint, author, id, passId, competitors.get(0), boat);
    }
}
