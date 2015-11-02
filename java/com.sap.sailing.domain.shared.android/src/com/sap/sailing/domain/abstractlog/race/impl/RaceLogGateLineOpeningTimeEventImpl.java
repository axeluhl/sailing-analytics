package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogGateLineOpeningTimeEventImpl extends RaceLogEventImpl implements RaceLogGateLineOpeningTimeEvent {

    private static final long serialVersionUID = 793529890804809490L;
    private final GateLineOpeningTimes gateLineOpeningTimes;

    public RaceLogGateLineOpeningTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint,
            AbstractLogEventAuthor author, Serializable pId, List<Competitor> pCompetitors, int pPassId,
            long gateLaunchStopTime, long golfDownTime) {
        super(createdAt, pTimePoint, author, pId, pCompetitors, pPassId);
        this.gateLineOpeningTimes = new GateLineOpeningTimes(gateLaunchStopTime, golfDownTime);
    }

    public RaceLogGateLineOpeningTimeEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId,
            long gateLaunchStopTime, long golfDownTime) {
        super(pTimePoint, author, pPassId);
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
