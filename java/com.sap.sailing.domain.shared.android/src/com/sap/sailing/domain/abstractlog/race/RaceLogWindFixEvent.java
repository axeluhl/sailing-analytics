package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.tracking.Wind;

/**
 * Contains a wind fix that the race officers documented in the Race Committee App ashore
 *
 */
public interface RaceLogWindFixEvent extends RaceLogEvent {

    /**
     * Returns the wind fix entered by the race committee
     */
    Wind getWindFix();
}
