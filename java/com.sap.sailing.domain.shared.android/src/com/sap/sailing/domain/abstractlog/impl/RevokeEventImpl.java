package com.sap.sailing.domain.abstractlog.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.RevokeEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class RevokeEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements RevokeEvent<VisitorT> {
    private static final long serialVersionUID = -30864810737555657L;
    
    private final Serializable revokedEventId;
    private final String reason;
    private final String revokedEventType;
    private final String revokedEventShortInfo;
    
    public RevokeEventImpl(AbstractLogEventAuthor author, AbstractLogEvent<?> toRevoke, String reason) {
        this(MillisecondsTimePoint.now(), MillisecondsTimePoint.now(), author, UUID.randomUUID(),
                toRevoke.getId(), toRevoke.getClass().getName(), toRevoke.getShortInfo(), reason);
    }
    
    public RevokeEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, Serializable revokedEventId, String revokedEventType,
            String revokedEventShortInfo, String reason) {
        super(createdAt, logicalTimePoint, author, pId);
        this.revokedEventId = revokedEventId;
        this.reason = reason;
        this.revokedEventType = revokedEventType;
        this.revokedEventShortInfo = revokedEventShortInfo;
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
