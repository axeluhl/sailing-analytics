package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogDependentStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDependentStartTimeEventSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class RaceLogDependentStartTimeEventDeserializer extends RaceLogRaceStatusEventDeserializer {
    
    public RaceLogDependentStartTimeEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        String raceColumnName = (String) object.get(RaceLogDependentStartTimeEventSerializer.FIELD_DEPDENDENT_ON_RACECOLUMN);
        String regattaLikeParentName = (String) object.get(RaceLogDependentStartTimeEventSerializer.FIELD_DEPDENDENT_ON_REGATTALIKE);
        String fleetName = (String) object.get(RaceLogDependentStartTimeEventSerializer.FIELD_DEPDENDENT_ON_FLEET);
        long startTimeDifferenceInMs = (long) object.get(RaceLogDependentStartTimeEventSerializer.FIELD_START_TIME_DIFFERENCE);
        Duration startTimeDifference = new MillisecondsDurationImpl(startTimeDifferenceInMs);
        RaceLogRaceStatusEvent event = (RaceLogRaceStatusEvent) super.deserialize(object, id, createdAt, author, timePoint, passId, competitors);
        SimpleRaceLogIdentifier dependentOnRace = new SimpleRaceLogIdentifierImpl(regattaLikeParentName, raceColumnName, fleetName);
        return new RaceLogDependentStartTimeEventImpl(event.getCreatedAt(), event.getLogicalTimePoint(), author, event.getId(), 
                event.getPassId(), dependentOnRace, startTimeDifference, event.getNextStatus());
    }
}
