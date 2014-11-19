package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogGateLineOpeningTimeEventImpl extends RaceLogEventImpl implements RaceLogGateLineOpeningTimeEvent {
   
    private static final long serialVersionUID = 793529890804809490L;
    private final GateLineOpeningTimes gateLineOpeningTimes;

    public RaceLogGateLineOpeningTimeEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, 
            long gateLaunchStopTime, long golfDownTime) {
        super(createdAt, author, pTimePoint, pId, pCompetitors, pPassId);
        this.gateLineOpeningTimes = new GateLineOpeningTimes(gateLaunchStopTime, golfDownTime);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public GateLineOpeningTimes getGateLineOpeningTimes() {
        return this.gateLineOpeningTimes;
    }

}
