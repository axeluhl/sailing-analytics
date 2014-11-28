package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.WithID;

/**
 * Special kind of {@link Track} for recording {@link RegattaLogEvent}s.
 * 
 * <p>
 * Use {@link RaceLog#getRawFixes()} to receive all events in a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Implementations should use the {@link RegattaLogEventComparator} for sorting its content.
 * </p>
 */
public interface RegattaLog extends AbstractLog<RegattaLogEvent, RegattaLogEventVisitor>, WithID {

}
