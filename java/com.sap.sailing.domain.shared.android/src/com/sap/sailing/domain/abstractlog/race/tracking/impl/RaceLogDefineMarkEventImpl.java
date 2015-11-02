package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.TimePoint;

public class RaceLogDefineMarkEventImpl extends RaceLogEventImpl implements RaceLogDefineMarkEvent {
    private static final long serialVersionUID = 277007856878002208L;
    
    private final Mark mark;
    
    public RaceLogDefineMarkEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, Mark mark) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        this.mark = mark;
    }
    
    public RaceLogDefineMarkEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int pPassId, Mark mark) {
        this(now(), logicalTimePoint, author, randId(), pPassId, mark);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Mark getMark() {
        return mark;
    }

}
