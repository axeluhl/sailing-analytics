package com.sap.sailing.domain.swisstimingadapter;

public interface Mark extends WithDescription {
    int getIndex();
    
    Iterable<String> getDevices();
}
