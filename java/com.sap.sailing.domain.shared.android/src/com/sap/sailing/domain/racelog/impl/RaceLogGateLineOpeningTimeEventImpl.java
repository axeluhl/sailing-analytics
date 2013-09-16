package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;

public class RaceLogGateLineOpeningTimeEventImpl extends RaceLogEventImpl implements RaceLogGateLineOpeningTimeEvent {
   
    private static final long serialVersionUID = 793529890804809490L;
    private Long gateLineOpeningTime;

    public RaceLogGateLineOpeningTimeEventImpl(TimePoint createdAt,
            TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, Long gateLineOpeningTime) {
        super(createdAt, pTimePoint, pId, pCompetitors, pPassId);
        this.gateLineOpeningTime = gateLineOpeningTime;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Long getGateLineOpeningTime() {
        return this.gateLineOpeningTime;
    }

}
