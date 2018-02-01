package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.server.RacingEventService;

public class RecordRegattaLogEventOnFlexibleLeaderboard extends AbstractRegattaLogOnFlexibleLeaderboardOperation<RegattaLogEvent> {
    private static final long serialVersionUID = -6362628649889525333L;
    
    private final RegattaLogEvent event;
    
    public RecordRegattaLogEventOnFlexibleLeaderboard(String flexibleLeaderboardName, RegattaLogEvent event) {
        super(flexibleLeaderboardName);
        this.event = event;
    }

    @Override
    public RegattaLogEvent internalApplyTo(RacingEventService toState) throws Exception {
        getRegattaLog(toState).add(event);
        return event;
    }
}
