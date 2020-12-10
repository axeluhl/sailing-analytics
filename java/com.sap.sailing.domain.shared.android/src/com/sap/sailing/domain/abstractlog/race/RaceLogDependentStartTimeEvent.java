package com.sap.sailing.domain.abstractlog.race;

import com.sap.sse.common.Duration;


public interface RaceLogDependentStartTimeEvent extends RaceLogRaceStatusEvent, RaceLogCourseAreaSpecification {

    Duration getStartTimeDifference();

    SimpleRaceLogIdentifier getDependentOnRaceIdentifier();

}
