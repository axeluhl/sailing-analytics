package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

/**
 * Closes the phase during which {@link RaceLogDeviceCompetitorMappingEventImpl}s actually result
 * in competitors being added, and indicates that the server should create a {@link TrackedRace}
 * using the information in this {@link RaceLog}.
 * 
 * @author Fredrik Teschke
 *
 */
public interface RaceLogStartTrackingEvent extends RaceLogEvent, Revokable {
}
