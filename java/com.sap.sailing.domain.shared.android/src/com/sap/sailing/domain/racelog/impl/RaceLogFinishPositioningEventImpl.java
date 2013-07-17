package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningEvent;

public abstract class RaceLogFinishPositioningEventImpl extends RaceLogEventImpl implements RaceLogFinishPositioningEvent {

    private static final long serialVersionUID = -8168584588697908309L;
    
    private final List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors;

    public RaceLogFinishPositioningEventImpl(TimePoint createdAt, TimePoint pTimePoint, Serializable pId,
            List<Competitor> pInvolvedBoats, int pPassId, List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        super(createdAt, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.positionedCompetitors = positionedCompetitors;
    }
    
    @Override
    public List<Triple<Serializable, String, MaxPointsReason>> getPositionedCompetitors() {
        return positionedCompetitors;
    }

}
