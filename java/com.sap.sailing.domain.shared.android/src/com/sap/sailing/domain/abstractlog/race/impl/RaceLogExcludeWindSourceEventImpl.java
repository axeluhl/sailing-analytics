package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogExcludeWindSourceEvent;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sse.common.TimePoint;

public class RaceLogExcludeWindSourceEventImpl extends RaceLogEventImpl implements RaceLogExcludeWindSourceEvent {
    private static final long serialVersionUID = 7879094280634905183L;
    
    private final WindSource windSourceToExclude;
    
    public RaceLogExcludeWindSourceEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, WindSource windSourceToExclude) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.windSourceToExclude = windSourceToExclude;
    }
    
    public RaceLogExcludeWindSourceEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int pPassId, WindSource windSourceToExclude) {
        this(now(), logicalTimePoint, author, randId(), pPassId, windSourceToExclude);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public WindSource getWindSourceToExclude() {
        return windSourceToExclude;
    }

    @Override
    public String getShortInfo() {
        return "windSourceToExclude=" + windSourceToExclude;
    }
}
