package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDenoteForTrackingEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogDenoteForTrackingEventDeserializer extends BaseRaceLogEventDeserializer {	
    private final SharedDomainFactory domainFactory;
    public RaceLogDenoteForTrackingEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer,
            SharedDomainFactory factory) {
        super(competitorDeserializer);
        this.domainFactory = factory;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
    	String raceName = (String) object.get(RaceLogDenoteForTrackingEventSerializer.FIELD_RACE_NAME);
    	BoatClass boatClass = domainFactory.getOrCreateBoatClass(
    			(String) object.get(RaceLogDenoteForTrackingEventSerializer.FIELD_BOAT_CLASS));
    	Serializable raceId = (Serializable) object.get(RaceLogDenoteForTrackingEventSerializer.FIELD_RACE_ID);
        
        return new RaceLogDenoteForTrackingEventImpl(createdAt, timePoint, author, id, passId, raceName, boatClass, raceId);
    }
}
