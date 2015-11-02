package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.TimePoint;

public class RaceLogWindFixEventImpl extends RaceLogEventImpl implements RaceLogWindFixEvent {
    private static final long serialVersionUID = 7879094280634905183L;
    
    private final Wind windFix;
    private final boolean isMagnetic;
    
    public RaceLogWindFixEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, Wind windFix, boolean isMagnetic) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.windFix = windFix;
        this.isMagnetic = isMagnetic;
    }
    
    public RaceLogWindFixEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int pPassId, Wind windFix, boolean isMagnetic) {
        this(now(), logicalTimePoint, author, randId(), pPassId, windFix, isMagnetic);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Wind getWindFix() {
        return windFix;
    }
    
    @Override
    public boolean isMagnetic() {
        return isMagnetic;
    }

    @Override
    public String getShortInfo() {
        return "windFix=" + windFix;
    }
}
