package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorEventDeserializer extends BaseRaceLogEventDeserializer {
    private final JsonDeserializer<CompetitorWithBoat> competitorWithBoatDeserializer;
    
    public RaceLogRegisterCompetitorEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer, JsonDeserializer<CompetitorWithBoat> competitorWithBoatDeserializer) {
        super(competitorDeserializer);
        this.competitorWithBoatDeserializer = competitorWithBoatDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        assert competitors.size() == 1 : "Expected exactly one competitor for RegisterCompetitorEvent";
        CompetitorWithBoat competitorWithBoat = competitorWithBoatDeserializer.deserialize(object);
    	return new RaceLogRegisterCompetitorEventImpl(createdAt, timePoint, author, id, passId, competitorWithBoat);
    }

}
