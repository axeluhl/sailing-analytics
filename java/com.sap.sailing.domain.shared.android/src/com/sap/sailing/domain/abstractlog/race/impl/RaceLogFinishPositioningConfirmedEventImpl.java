package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogFinishPositioningConfirmedEventImpl extends RaceLogFinishPositioningEventImpl implements
        RaceLogFinishPositioningConfirmedEvent {

    private static final long serialVersionUID = 5028181339200048499L;

    public RaceLogFinishPositioningConfirmedEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pCompetitors, int pPassId,
            CompetitorResults positionedCompetitors) {
        super(createdAt, pTimePoint, author, pId, pCompetitors, pPassId, positionedCompetitors);
    }

    public RaceLogFinishPositioningConfirmedEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId,
            CompetitorResults positionedCompetitors) {
        super(pTimePoint, author, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
