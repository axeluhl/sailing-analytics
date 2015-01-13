package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.RevokeEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sse.common.TimePoint;

public class RegattaLogRevokeEventImpl extends RevokeEventImpl<RegattaLogEventVisitor> implements RegattaLogRevokeEvent {
    private static final long serialVersionUID = -3470191515219206588L;
    
    public RegattaLogRevokeEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo,
            String reason) {
        super(createdAt, author, logicalTimePoint, pId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }
    
    public RegattaLogRevokeEventImpl(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason) {
        super(author, toRevoke, reason);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
