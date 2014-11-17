package com.sap.sailing.domain.abstractlog.regatta;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventRestoreFactoryImpl;
import com.sap.sailing.domain.common.TimePoint;

public interface RegattaLogEventRestoreFactory extends RegattaLogEventFactory {
    RegattaLogEventRestoreFactory INSTANCE = new RegattaLogEventRestoreFactoryImpl();
    
    RegattaLogRevokeEvent createRevokeEvent(TimePoint createdAt, AbstractLogEventAuthor author,
    		TimePoint logicalTimePoint, Serializable pId, Serializable revokedEventId,
    		String revokedEventType, String revokedEventShortInfo, String reason);
}
