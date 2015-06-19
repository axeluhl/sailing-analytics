package com.sap.sailing.domain.abstractlog.race;

import com.sap.sse.common.Duration;


public interface RaceLogDependentStartTimeEvent extends RaceLogRaceStatusEvent {

    Duration getStartTimeDifference();

    SimpleRaceLogIdentifier getDependentOnRaceIdentifier();

}
