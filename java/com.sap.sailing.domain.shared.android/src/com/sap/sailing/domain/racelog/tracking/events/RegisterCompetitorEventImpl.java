package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sse.common.TimePoint;

public class RegisterCompetitorEventImpl extends RaceLogEventImpl implements RegisterCompetitorEvent {
    private static final long serialVersionUID = -30864810737555657L;
    
    public RegisterCompetitorEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Competitor competitor) {
        super(createdAt, author, logicalTimePoint, pId, Collections.singletonList(competitor), pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Competitor getCompetitor() {
        return getInvolvedBoats().get(0);
    } 

    @Override
    public String getShortInfo() {
        return "competitor: " + getCompetitor().toString();
    }
}
