package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventComparator;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.tracking.Track;

/**
 * Special kind of {@link Track} for recording {@link RaceLogEvent}s.
 * 
 * <p>
 * Keeps track of the {@link RaceLogEvent}'s pass and returns only the events of the current pass on
 * {@link RaceLog#getFixes()}. Use {@link RaceLog#getRawFixes()} to receive all events in a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Implementations should use the {@link RaceLogEventComparator} for sorting its content.
 * </p>
 */
public interface RaceLog extends AbstractLog<RaceLogEvent>, WithID {
   
}
