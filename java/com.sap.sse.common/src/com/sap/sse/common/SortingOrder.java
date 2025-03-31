package com.sap.sse.common;

public enum SortingOrder {
    NONE, ASCENDING, DESCENDING;
    
    public boolean isAscending() {
        return this == ASCENDING;
    }

    public boolean isDescending() {
        return this == DESCENDING;
    }
}
