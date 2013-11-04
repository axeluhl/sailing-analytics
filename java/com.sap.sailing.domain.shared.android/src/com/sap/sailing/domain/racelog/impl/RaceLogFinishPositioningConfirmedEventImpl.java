package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;

public class RaceLogFinishPositioningConfirmedEventImpl extends RaceLogFinishPositioningEventImpl implements RaceLogFinishPositioningConfirmedEvent {
   
    private static final long serialVersionUID = 5028181339200048499L;
    
    public RaceLogFinishPositioningConfirmedEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        super(createdAt, author, pTimePoint, pId, pCompetitors, pPassId, positionedCompetitors);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
