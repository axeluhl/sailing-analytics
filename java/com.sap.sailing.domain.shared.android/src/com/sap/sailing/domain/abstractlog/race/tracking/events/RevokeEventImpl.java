package com.sap.sailing.domain.abstractlog.race.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class RevokeEventImpl extends RaceLogEventImpl implements RevokeEvent {
    private static final long serialVersionUID = -30864810737555657L;
    
    private final Serializable revokedEventId;
    private final String reason;
    private final String revokedEventType;
    private final String revokedEventShortInfo;
    
    public RevokeEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Serializable revokedEventId, String revokedEventType,
            String revokedEventShortInfo, String reason) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
        this.revokedEventId = revokedEventId;
        this.reason = reason;
        this.revokedEventType = revokedEventType;
        this.revokedEventShortInfo = revokedEventShortInfo;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Serializable getRevokedEventId() {
        return revokedEventId;
    } 
    
    @Override
    public String getShortInfo() {
        if (revokedEventShortInfo == null) {
            return "No event revoked";
        } else {
            return "Revoked " + revokedEventType + "(" + revokedEventShortInfo + ")"
                    + reason == null ? "" : (" due to " + reason);
        }
    }
    
    @Override
    public String getRevokedEventType() {
        return revokedEventType;
    }

    @Override
    public String getRevokedEventShortInfo() {
        return revokedEventShortInfo;
    }

    @Override
    public String getReason() {
        return reason;
    }                
}
