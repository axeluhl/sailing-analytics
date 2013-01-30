package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetsData extends Serializable {
    
    Number[] getValues();
    
    int getDataCount();
    
    boolean isComplete();
    
    Integer[] getDataCountPerAngle();

}
