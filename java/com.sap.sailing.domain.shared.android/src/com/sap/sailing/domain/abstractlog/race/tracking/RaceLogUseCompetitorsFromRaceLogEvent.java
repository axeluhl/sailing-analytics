package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogUsesOwnCompetitorsAnalyzer;

/**
 * This is a marker event, which marks a {@link RaceLog} for using it's own competitor registrations. When present,
 * competitors are not registered on the RegattaLog corresponding to the RaceLog, but on the RaceLog itself. This is for
 * example used for split fleets.
 * 
 * See bug2851.
 * 
 * @see RaceLogUsesOwnCompetitorsAnalyzer
 * @author Jan Bross (D056848)
 *
 */
public interface RaceLogUseCompetitorsFromRaceLogEvent extends RaceLogEvent, Revokable {
}
