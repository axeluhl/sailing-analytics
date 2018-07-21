package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogFinishPositioningListChangedEventImpl extends RaceLogFinishPositioningEventImpl implements
        RaceLogFinishPositioningListChangedEvent {

    private static final long serialVersionUID = -8167472925561954739L;

    public RaceLogFinishPositioningListChangedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int pPassId,
            CompetitorResults positionedCompetitors) {
        super(createdAt, pTimePoint, author, pId, pPassId, positionedCompetitors);
    }

    public RaceLogFinishPositioningListChangedEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author,
            int pPassId, CompetitorResults positionedCompetitors) {
        super(pTimePoint, author, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
