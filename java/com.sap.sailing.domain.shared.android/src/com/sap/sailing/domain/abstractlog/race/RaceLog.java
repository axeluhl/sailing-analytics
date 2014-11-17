package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.common.WithID;

public interface RaceLog extends AbstractLog<RaceLogEvent, RaceLogEventVisitor>, WithID {
    public static final int DefaultPassId = 0;
    
    /**
     * Gets the current pass id.
     * 
     * @return the pass id.
     */
    int getCurrentPassId();
}
