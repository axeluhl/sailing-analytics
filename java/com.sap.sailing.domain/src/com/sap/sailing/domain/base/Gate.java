package com.sap.sailing.domain.base;

public interface Gate extends ControlPoint {
    SingleMark getLeft();
    
    SingleMark getRight();
}
