package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogRevokeEvent;
import com.sap.sse.common.TimePoint;

public class RegattaLogEventRestoreFactoryImpl extends RegattaLogEventFactoryImpl implements RegattaLogEventRestoreFactory {
    @Override
    public RegattaLogRevokeEvent createRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Serializable revokedEventId, String revokedEventType, String revokedEventShortInfo, String reason) {
        return new RegattaLogRevokeEventImpl(createdAt, author, logicalTimePoint, pId, revokedEventId, revokedEventType, revokedEventShortInfo, reason);
    }
}
