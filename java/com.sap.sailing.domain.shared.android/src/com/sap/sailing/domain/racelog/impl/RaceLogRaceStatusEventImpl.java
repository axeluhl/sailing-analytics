package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogRaceStatusEventImpl extends RaceLogEventImpl implements RaceLogRaceStatusEvent {
    private static final long serialVersionUID = -8809758843066724482L;

    private final RaceLogRaceStatus nextStatus;

    public RaceLogRaceStatusEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint pTimePoint,
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
