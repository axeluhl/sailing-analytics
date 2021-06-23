package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.common.WindSource;

/**
 * Tells a single wind source to exclude for the race in whose race log this event occurs. Events of this
 * type are independent of the pass. They are revokable. Multiple such events may occur in the same race log,
 * excluding multiple wind sources.
 */
public interface RaceLogExcludeWindSourceEvent extends RaceLogEvent, Revokable {

    /**
     * Returns the wind source to exclude
     */
    WindSource getWindSourceToExclude();
}
