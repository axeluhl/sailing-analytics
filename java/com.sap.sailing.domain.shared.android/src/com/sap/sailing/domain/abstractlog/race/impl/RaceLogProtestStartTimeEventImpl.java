package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;

public class RaceLogProtestStartTimeEventImpl extends RaceLogEventImpl implements RaceLogProtestStartTimeEvent {
    private static final long serialVersionUID = -1800827552916395996L;

    private final TimeRange protestTime;
    
    public RaceLogProtestStartTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, TimeRange protestTime) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        assert protestTime != null && protestTime.from() != null && protestTime.to() != null;
        this.protestTime = protestTime;
    }

    public RaceLogProtestStartTimeEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            TimeRange protestTime) {
        this(now(), logicalTimePoint, author, randId(), pPassId, protestTime);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TimeRange getProtestTime() {
        return protestTime;
    }

    @Override
    public String getShortInfo() {
        return "protestStartTime=" + protestTime.from() + ", protestEndTime=" + protestTime.to();
    }

}
