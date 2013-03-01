package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.common.TimePoint;

public interface RaceLogStartTimeEvent extends RaceLogRaceStatusEvent {

    TimePoint getStartTime();
}
