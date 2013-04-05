package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class RaceLogFinishPositioningListChangedEventImpl extends RaceLogEventImpl implements RaceLogFinishPositioningListChangedEvent {
   
    private static final long serialVersionUID = -8167472925561954739L;

    public RaceLogFinishPositioningListChangedEventImpl(TimePoint createdAt,
            TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId) {
        super(createdAt, pTimePoint, pId, pCompetitors, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
