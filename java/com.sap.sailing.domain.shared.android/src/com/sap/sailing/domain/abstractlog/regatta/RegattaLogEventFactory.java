package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventFactoryImpl;

public interface RegattaLogEventFactory {
    RegattaLogEventFactory INSTANCE = new RegattaLogEventFactoryImpl();
    
    RegattaLogRevokeEvent createRevokeEvent(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason);
}
