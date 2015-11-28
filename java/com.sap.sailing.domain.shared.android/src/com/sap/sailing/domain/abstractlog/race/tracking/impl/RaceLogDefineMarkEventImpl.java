package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.AbstractDefineMarkEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.TimePoint;

@Deprecated //see bug2851
public class RaceLogDefineMarkEventImpl extends AbstractDefineMarkEventImpl<RaceLogEventVisitor> implements RaceLogDefineMarkEvent {
    private static final long serialVersionUID = 277007856878002208L;

    private RaceLogEventDataImpl raceLogEventData;
    
    public RaceLogDefineMarkEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Mark mark) {
        super(createdAt, author, logicalTimePoint, pId, mark);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<Competitor> getInvolvedBoats() {
        return Collections.<Competitor>emptyList();
    }

    @Override
    public String getShortInfo() {
        return mark.toString();
    }

}
