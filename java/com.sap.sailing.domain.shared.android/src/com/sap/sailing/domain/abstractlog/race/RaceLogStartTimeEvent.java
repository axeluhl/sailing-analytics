package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

/**
 * A {@link RaceLogStartTimeEvent} denotes a new start time.
 * 
 * Each {@link RaceLogStartTimeEvent} is also a {@link RaceLogRaceStatusEvent} with
 * {@link RaceLogRaceStatusEvent#getNextStatus()} returning {@link RaceLogRaceStatus#SCHEDULED}.
 */
public interface RaceLogStartTimeEvent extends RaceLogRaceStatusEvent {

    TimePoint getStartTime();
}
