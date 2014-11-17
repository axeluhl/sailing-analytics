package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class RaceLogFinishPositioningConfirmedEventImpl extends RaceLogFinishPositioningEventImpl implements RaceLogFinishPositioningConfirmedEvent {
   
    private static final long serialVersionUID = 5028181339200048499L;
    
    public RaceLogFinishPositioningConfirmedEventImpl(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, CompetitorResults positionedCompetitors) {
        super(createdAt, author, pTimePoint, pId, pCompetitors, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
