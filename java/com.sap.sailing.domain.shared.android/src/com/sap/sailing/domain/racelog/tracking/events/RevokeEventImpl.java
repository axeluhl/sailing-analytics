package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;

public class RevokeEventImpl extends RaceLogEventImpl implements RevokeEvent {
    private static final long serialVersionUID = -30864810737555657L;
    
    private final Serializable revokedEventId;
    
    public RevokeEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Serializable revokedEventId) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
        this.revokedEventId = revokedEventId;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Serializable getRevokedEventId() {
        return revokedEventId;
    } 
}
