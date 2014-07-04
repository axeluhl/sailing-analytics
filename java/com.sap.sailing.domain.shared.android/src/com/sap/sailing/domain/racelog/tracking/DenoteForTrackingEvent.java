package com.sap.sailing.domain.racelog.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * The existence of this event in a {@link RaceLog} indicates that the necessary data
 * for creating this race will be accumulated in this RaceLog. Once it is present, the
 * actual {@link TrackedRace} can then be created from this information, and fixes for
 * competitors and marks that arrive on the server, for which a corresponding
 * {@link DeviceMappingEvent} exists will be added to the race.
 * @author Fredrik Teschke
 *
 */
public interface DenoteForTrackingEvent extends RaceLogEvent, Revokable {
    String getRaceName();
    BoatClass getBoatClass();
    Serializable getRaceId();
}
