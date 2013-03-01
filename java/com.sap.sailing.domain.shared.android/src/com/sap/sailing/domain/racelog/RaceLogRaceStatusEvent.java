package com.sap.sailing.domain.racelog;

/**
 * Events to indicate the new race status which is valid for all events
 * arriving after this event (on the same pass).
 */
public interface RaceLogRaceStatusEvent extends RaceLogEvent {

    RaceLogRaceStatus getNextStatus();

}
