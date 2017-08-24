package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

/**
 * This is a marker event, which marks a {@link RaceLog} for using it's own boat registrations. When present,
 * boats are not registered on the RegattaLog corresponding to the RaceLog, but on the RaceLog itself.
 * 
 * @author Frank Mittag
 *
 */
public interface RaceLogUseCompetitorsAndBoatsFromRaceLogEvent extends RaceLogEvent, Revokable {
}
