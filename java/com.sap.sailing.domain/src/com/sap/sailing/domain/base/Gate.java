package com.sap.sailing.domain.base;

public interface Gate extends ControlPoint {
    Buoy getLeft();
    
    Buoy getRight();
}
