package com.sap.sailing.domain.swisstimingadapter;

public interface Mark extends WithDescription {
    public enum MarkType { MANUAL, LINE_CROSSING, BUOY_ROUNDING };
    
    int getIndex();
    
    Iterable<String> getDevices();
    
    MarkType getMarkType();
}
