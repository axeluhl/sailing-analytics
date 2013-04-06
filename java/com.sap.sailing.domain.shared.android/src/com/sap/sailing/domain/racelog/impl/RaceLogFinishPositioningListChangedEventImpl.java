package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class RaceLogFinishPositioningListChangedEventImpl extends RaceLogEventImpl implements RaceLogFinishPositioningListChangedEvent {
   
    private static final long serialVersionUID = -8167472925561954739L;
    
    private List<Pair<Competitor, MaxPointsReason>> positionedCompetitors;

    public RaceLogFinishPositioningListChangedEventImpl(TimePoint createdAt,
            TimePoint pTimePoint, Serializable pId, List<Competitor> competitors, int pPassId, List<Pair<Competitor, MaxPointsReason>> positionedCompetitors) {
        super(createdAt, pTimePoint, pId, competitors, pPassId);
        this.positionedCompetitors = positionedCompetitors;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public List<Pair<Competitor, MaxPointsReason>> getPositionedCompetitors() {
        return positionedCompetitors;
    }

}
