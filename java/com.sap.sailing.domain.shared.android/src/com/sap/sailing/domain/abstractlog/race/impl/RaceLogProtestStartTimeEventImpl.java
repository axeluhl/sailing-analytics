package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RaceLogProtestStartTimeEventImpl extends RaceLogEventImpl implements RaceLogProtestStartTimeEvent {
    private static final long serialVersionUID = -1800827552916395996L;

    private final TimePoint protestStartTime;
    private final Duration protestDuration;

    public RaceLogProtestStartTimeEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, TimePoint protestStartTime, Duration protestDuration) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.protestStartTime = protestStartTime;
        this.protestDuration = protestDuration;
    }

    public RaceLogProtestStartTimeEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            TimePoint protestStartTime, Duration protestDuration) {
        this(now(), logicalTimePoint, author, randId(), pPassId, protestStartTime, protestDuration);
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
    public Duration getProtestDuration() {
        return protestDuration;
    }

    @Override
    public String getShortInfo() {
        return "protestStartTime=" + protestStartTime + ", protestDuration=" + protestDuration;
    }

}
