package com.sap.sailing.domain.common;

public enum SortingOrder {
    NONE, ASCENDING, DESCENDING;
    
    public boolean isAscending() {
        return this == ASCENDING;
    }

    public boolean isDescending() {
        return this == DESCENDING;
    }
}
