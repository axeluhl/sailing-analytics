package com.sap.sailing.domain.abstractlog.race.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.base.BoatClass;

/**
 * The existence of this event in a {@link RaceLog} indicates that the necessary data
 * for creating this race will be accumulated in this RaceLog. Once it is present, the
 * actual {@link TrackedRace} can then be created from this information, and fixes for
 * competitors and marks that arrive on the server, for which a corresponding
 * {@link RegattaLogDeviceMappingEvent} exists will be added to the race.
 * @author Fredrik Teschke
 *
 */
public interface RaceLogDenoteForTrackingEvent extends RaceLogEvent, Revokable {
    String getRaceName();
    BoatClass getBoatClass();
    Serializable getRaceId();
}
