package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.TimePoint;

public class RaceLogDefineMarkEventImpl extends RaceLogEventImpl implements RaceLogDefineMarkEvent {
    private static final long serialVersionUID = 277007856878002208L;
    
    private final Mark mark;
    
    public RaceLogDefineMarkEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Mark mark) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
         this.mark = mark;
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
