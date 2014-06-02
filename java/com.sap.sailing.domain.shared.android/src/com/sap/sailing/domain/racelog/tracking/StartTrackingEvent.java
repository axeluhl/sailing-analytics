package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;
import com.sap.sailing.domain.racelog.tracking.events.DeviceCompetitorMappingEventImpl;

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
