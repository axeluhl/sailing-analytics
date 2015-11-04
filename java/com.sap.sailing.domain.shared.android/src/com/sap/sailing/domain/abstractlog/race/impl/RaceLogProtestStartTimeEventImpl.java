package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogProtestStartTimeEventImpl extends RaceLogEventImpl implements RaceLogProtestStartTimeEvent {
    private static final long serialVersionUID = -1800827552916395996L;

    private final TimePoint protestStartTime;

    public RaceLogProtestStartTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, TimePoint protestStartTime) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.protestStartTime = protestStartTime;
    }

    public RaceLogProtestStartTimeEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            TimePoint protestStartTime) {
        this(now(), logicalTimePoint, author, randId(), pPassId, protestStartTime);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TimePoint getProtestStartTime() {
        return protestStartTime;
    }

    @Override
    public String getShortInfo() {
        return "protestStartTime=" + protestStartTime;
    }

}
