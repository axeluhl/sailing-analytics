package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningConfirmedEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogFinishPositioningConfirmedEventDeserializer extends RaceLogFinishPositioningEventDeserializer {
    
    public RaceLogFinishPositioningConfirmedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    protected RaceLogFinishPositioningConfirmedEventImpl createRaceLogFinishPositioningEvent(Serializable id,
            TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId,
            CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningConfirmedEventImpl(createdAt, timePoint, author, id, passId, positionedCompetitors);
    }
}
