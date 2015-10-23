package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.TimePoint;

public class RaceLogWindFixEventImpl extends RaceLogEventImpl implements RaceLogWindFixEvent {
    private static final long serialVersionUID = 7879094280634905183L;
    
    private final Wind windFix;
    private final boolean isMagnetic;
    
    public RaceLogWindFixEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint pTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Wind windFix, boolean isMagnetic) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.windFix = windFix;
        this.isMagnetic = isMagnetic;
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
