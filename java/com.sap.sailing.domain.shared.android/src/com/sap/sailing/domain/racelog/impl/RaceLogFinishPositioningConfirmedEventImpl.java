package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogFinishPositioningConfirmedEventImpl extends RaceLogFinishPositioningEventImpl implements RaceLogFinishPositioningConfirmedEvent {
   
    private static final long serialVersionUID = 5028181339200048499L;
    
    public RaceLogFinishPositioningConfirmedEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, CompetitorResults positionedCompetitors) {
        super(createdAt, author, pTimePoint, pId, pCompetitors, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
