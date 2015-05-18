package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sse.common.Duration;


public interface RaceLogDependentStartTimeEvent extends RaceLogRaceStatusEvent {

    Fleet getDependentOnFleet();

    Duration getStartTimeDifference();

}
