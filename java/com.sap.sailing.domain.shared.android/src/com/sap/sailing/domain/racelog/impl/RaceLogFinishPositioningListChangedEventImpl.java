package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class RaceLogFinishPositioningListChangedEventImpl extends RaceLogFinishPositioningEventImpl implements RaceLogFinishPositioningListChangedEvent {
   
    private static final long serialVersionUID = -8167472925561954739L;
    
    public RaceLogFinishPositioningListChangedEventImpl(TimePoint createdAt,
            TimePoint pTimePoint, Serializable pId, List<Competitor> competitors, int pPassId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        super(createdAt, pTimePoint, pId, competitors, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
