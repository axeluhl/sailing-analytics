package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.RevokeEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogRevokeEventImpl extends RevokeEventImpl<RaceLogEventVisitor> implements RaceLogRevokeEvent {
    private final int passId;
    
    public RaceLogRevokeEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int passId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo,
            String reason) {
        super(createdAt, author, logicalTimePoint, pId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
        this.passId = passId; 
    }

    private static final long serialVersionUID = 1886722815573499311L;

    @Override
    public int getPassId() {
        return passId;
    }

    @Override
    public List<Competitor> getInvolvedBoats() {
        return Collections.emptyList();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
