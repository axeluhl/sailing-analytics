package com.sap.sailing.domain.swisstimingadapter;

import java.io.Serializable;

public interface Mark extends WithDescription {
    public enum MarkType { MANUAL, LINE_CROSSING, BUOY_ROUNDING };
    
    int getIndex();
    
    Iterable<Serializable> getDeviceIds();
    
    MarkType getMarkType();
}
