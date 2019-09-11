package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

public interface ORCScratchBoatEvent extends RaceLogEvent, Revokable {
    Serializable getCompetitorId();
}
