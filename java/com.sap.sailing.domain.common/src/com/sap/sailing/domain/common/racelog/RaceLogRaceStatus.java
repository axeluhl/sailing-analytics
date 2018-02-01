package com.sap.sailing.domain.common.racelog;

/**
 * The status of a race in a race log
 * @author D053502
 * 
 */
public enum RaceLogRaceStatus {
    UNKNOWN(0), UNSCHEDULED(1), PRESCHEDULED(2), SCHEDULED(3), STARTPHASE(4), RUNNING(5), FINISHING(6), FINISHED(7);

    /** the order number represents the natural order of the states */
    private int orderNumber;
    
    RaceLogRaceStatus(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public static boolean isPreRunning(RaceLogRaceStatus status) {
        return status != null &&
            (status.equals(SCHEDULED) || status.equals(STARTPHASE));
    }
    
    public static boolean isActive(RaceLogRaceStatus status) {
        return status != null &&
            (status.equals(PRESCHEDULED) || status.equals(SCHEDULED) || status.equals(STARTPHASE) || status.equals(RUNNING) || status.equals(FINISHING));

    }

    public static boolean isRunningOrFinished(RaceLogRaceStatus status) {
        return status != null &&
            (status.equals(STARTPHASE) || status.equals(RUNNING) || status.equals(FINISHING) || status.equals(FINISHED));
    }

    public int getOrderNumber() {
        return orderNumber;
    }
}
