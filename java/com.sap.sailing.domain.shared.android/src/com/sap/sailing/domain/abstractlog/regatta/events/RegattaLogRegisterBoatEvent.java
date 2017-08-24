package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Boat;

public interface RegattaLogRegisterBoatEvent extends RegattaLogEvent, Revokable {
    Boat getBoat();
}
