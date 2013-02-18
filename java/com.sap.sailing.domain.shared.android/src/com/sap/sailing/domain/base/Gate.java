package com.sap.sailing.domain.base;

public interface Gate extends ControlPoint {
    Mark getLeft();
    
    Mark getRight();
}
