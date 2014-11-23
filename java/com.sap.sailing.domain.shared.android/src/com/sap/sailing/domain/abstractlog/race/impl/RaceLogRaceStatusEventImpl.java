package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.TimePoint;

public class RaceLogRaceStatusEventImpl extends RaceLogEventImpl implements RaceLogRaceStatusEvent {
    private static final long serialVersionUID = -8809758843066724482L;

    private final RaceLogRaceStatus nextStatus;

    public RaceLogRaceStatusEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, RaceLogRaceStatus nextStatus) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.nextStatus = nextStatus;
    }

    @Override
    public RaceLogRaceStatus getNextStatus() {
        return nextStatus;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "nextStatus=" + nextStatus;
    }
}
