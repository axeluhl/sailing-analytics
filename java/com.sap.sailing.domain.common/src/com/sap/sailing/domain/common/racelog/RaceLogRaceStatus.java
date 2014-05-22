package com.sap.sailing.domain.common.racelog;

/**
 * The status of a race in a race log
 * @author D053502
 * 
 */
public enum RaceLogRaceStatus {
    UNKNOWN(0), UNSCHEDULED(1), SCHEDULED(2), STARTPHASE(3), RUNNING(4), FINISHING(5), FINISHED(6);

    /** the order number represents the natural order of the states */
    private int orderNumber;
    
    RaceLogRaceStatus(int orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public static boolean isActive(RaceLogRaceStatus status) {
        if (status == null) {
            return false;
        }

        return status.equals(SCHEDULED) || status.equals(STARTPHASE) || status.equals(RUNNING) || status.equals(FINISHING);
    }

    public int getOrderNumber() {
        return orderNumber;
    }
}
