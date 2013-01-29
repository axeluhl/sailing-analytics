package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Map;

public interface PolarSheetsData extends Serializable {
    
    Map<Integer, Double> getData();
    
    boolean isComplete();

}
