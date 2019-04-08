package com.sap.sailing.domain.abstractlog.regatta;

import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sse.common.WithID;
/**
 * This log gathers information for regattas. {@link FlexibleLeaderboard}s are considered
 * workarounds for regattas in special cases. To expose a uniform interface, every {@link Leaderboard}
 * therefore can be asked for its {@link Leaderboard#getRegattaLog RegattaLog}. A {@link RegattaLeaderboard}
 * uses the log supplied by its {@code Regatta}, whereas a {@link FlexibleLeaderboard} manages the log itself.
 */
public interface RegattaLog extends AbstractLog<RegattaLogEvent, RegattaLogEventVisitor>, WithID {
    public void revokeDefineMarkEventAndRelatedDeviceMappings(final RegattaLogDefineMarkEvent event, final AbstractLogEventAuthor author, final Logger logger);
}
