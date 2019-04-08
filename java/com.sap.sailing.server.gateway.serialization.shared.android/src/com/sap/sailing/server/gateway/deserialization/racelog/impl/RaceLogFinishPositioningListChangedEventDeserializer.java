package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningListChangedEventImpl;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogFinishPositioningListChangedEventDeserializer extends RaceLogFinishPositioningEventDeserializer {
    
    public RaceLogFinishPositioningListChangedEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogFinishPositioningEvent createRaceLogFinishPositioningEvent(Serializable id,
            TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId,
            CompetitorResults positionedCompetitors) {
        return new RaceLogFinishPositioningListChangedEventImpl(createdAt, timePoint, author, id, passId, positionedCompetitors);
    }
}
