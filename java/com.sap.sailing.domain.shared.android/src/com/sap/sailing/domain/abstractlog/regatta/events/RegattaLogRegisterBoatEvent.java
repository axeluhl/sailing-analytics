package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Boat;

/**
* The event registers a standalone {@link Boat} on a regatta.
*/
public interface RegattaLogRegisterBoatEvent extends RegattaLogEvent, Revokable {
    Boat getBoat();
}
