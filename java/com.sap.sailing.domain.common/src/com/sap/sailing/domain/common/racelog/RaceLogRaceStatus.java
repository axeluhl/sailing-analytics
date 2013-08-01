package com.sap.sailing.domain.common.racelog;

/**
 * @author D053502
 * 
 */
public enum RaceLogRaceStatus {
    UNKNOWN, UNSCHEDULED, SCHEDULED, STARTPHASE, RUNNING, FINISHING, FINISHED;

    public static boolean isActive(RaceLogRaceStatus status) {
        if (status == null) {
            return false;
        }

        return status.equals(SCHEDULED) || status.equals(STARTPHASE) || status.equals(RUNNING)
                || status.equals(FINISHING);
    }
}
