package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogStartTimeEventImpl extends RaceLogRaceStatusEventImpl implements RaceLogStartTimeEvent {

    private static final long serialVersionUID = 8185811395997196162L;
    private final TimePoint startTime;

    public RaceLogStartTimeEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, TimePoint pStartTime) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId, RaceLogRaceStatus.SCHEDULED);
        this.startTime = pStartTime;
    }

    @Override
    public TimePoint getStartTime() {
        return startTime;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "startTime=" + startTime;
    }

}
