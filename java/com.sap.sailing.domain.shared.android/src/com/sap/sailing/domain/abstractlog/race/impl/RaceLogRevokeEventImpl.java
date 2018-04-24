package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.RevokeEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogRevokeEventImpl extends RevokeEventImpl<RaceLogEventVisitor> implements RaceLogRevokeEvent {
    private static final long serialVersionUID = 1886722815573499311L;
    private final RaceLogEventData raceLogEventData;
    
    public RaceLogRevokeEventImpl(AbstractLogEventAuthor author, int passId, RaceLogEvent toRevoke, String reason) {
        super(author, toRevoke, reason);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }
    
    public RaceLogRevokeEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int passId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo,
            String reason) {
        super(createdAt, logicalTimePoint, author, pId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
    }


    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public <T extends Competitor> List<T> getInvolvedCompetitors() {
        return raceLogEventData.getInvolvedCompetitors();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String toString() {
        return raceLogEventData.toString(); 
    }
}
