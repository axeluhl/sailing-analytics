package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventFactory;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogRevokeEvent;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogEventFactoryImpl implements RegattaLogEventFactory {

    @Override
    public RegattaLogRevokeEvent createRevokeEvent(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason) {
        return new RegattaLogRevokeEventImpl(MillisecondsTimePoint.now(), author, MillisecondsTimePoint.now(),
                UUID.randomUUID(), toRevoke.getId(), toRevoke.getClass().getName(), toRevoke.getShortInfo(), reason);
    }
}
