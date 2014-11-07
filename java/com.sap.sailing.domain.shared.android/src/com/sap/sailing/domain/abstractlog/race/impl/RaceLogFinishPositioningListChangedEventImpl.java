package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class RaceLogFinishPositioningListChangedEventImpl extends RaceLogFinishPositioningEventImpl implements RaceLogFinishPositioningListChangedEvent {
   
    private static final long serialVersionUID = -8167472925561954739L;
    
    public RaceLogFinishPositioningListChangedEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> competitors, int pPassId, CompetitorResults positionedCompetitors) {
        super(createdAt, author, pTimePoint, pId, competitors, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
