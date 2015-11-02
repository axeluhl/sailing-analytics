package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.TimePoint;

public class RaceLogStartTimeEventImpl extends RaceLogRaceStatusEventImpl implements RaceLogStartTimeEvent {

    private static final long serialVersionUID = 8185811395997196162L;
    private final TimePoint startTime;

    public RaceLogStartTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, TimePoint pStartTime) {
        super(createdAt, pTimePoint, author, pId, pPassId, RaceLogRaceStatus.SCHEDULED);
        this.startTime = pStartTime;
    }

    public RaceLogStartTimeEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId,
            TimePoint pStartTime) {
        super(pTimePoint, author, pPassId, RaceLogRaceStatus.SCHEDULED);
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
