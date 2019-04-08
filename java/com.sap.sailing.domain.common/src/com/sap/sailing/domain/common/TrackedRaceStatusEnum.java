package com.sap.sailing.domain.common;

public enum TrackedRaceStatusEnum { PREPARED(0), LOADING(1), TRACKING(2), FINISHED(3), ERROR(4), REMOVED(5);
    
    private final int order;

    private TrackedRaceStatusEnum(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
    
}