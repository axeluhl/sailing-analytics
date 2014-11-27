package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sse.common.TimePoint;

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
