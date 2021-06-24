package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.common.WindSource;

/**
 * Tells all wind sources to exclude for the race in whose race log this event occurs. Events of this
 * type are independent of the pass. They are revokable. The last unrevoked event of this type in a log
 * defines the wind sources to exclude.
 */
public interface RaceLogExcludeWindSourcesEvent extends RaceLogEvent, Revokable {

    /**
     * Returns the wind sources to exclude
     */
    Iterable<WindSource> getWindSourcesToExclude();
}
