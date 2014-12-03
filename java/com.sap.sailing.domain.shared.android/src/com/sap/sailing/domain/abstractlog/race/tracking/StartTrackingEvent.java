package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.events.DeviceCompetitorMappingEventImpl;

/**
 * Closes the phase during which {@link DeviceCompetitorMappingEventImpl}s actually result
 * in competitors being added, and indicates that the server should create a {@link TrackedRace}
 * using the information in this {@link RaceLog}.
 * 
 * @author Fredrik Teschke
 *
 */
public interface StartTrackingEvent extends RaceLogEvent, Revokable {
}
