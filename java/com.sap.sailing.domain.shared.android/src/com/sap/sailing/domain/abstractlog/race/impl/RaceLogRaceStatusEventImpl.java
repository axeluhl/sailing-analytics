package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.TimePoint;

public class RaceLogRaceStatusEventImpl extends RaceLogEventImpl implements RaceLogRaceStatusEvent {
    private static final long serialVersionUID = -8809758843066724482L;

    private final RaceLogRaceStatus nextStatus;

    public RaceLogRaceStatusEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, RaceLogRaceStatus nextStatus) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.nextStatus = nextStatus;
    }

    public RaceLogRaceStatusEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            RaceLogRaceStatus nextStatus) {
        this(now(), logicalTimePoint, author, randId(), pPassId, nextStatus);
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
