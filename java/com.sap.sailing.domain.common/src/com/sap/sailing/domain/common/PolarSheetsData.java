package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetsData extends Serializable {
    
    Number[][] getAveragedPolarDataByWindSpeed();
    
    int getDataCount();
    
    boolean isComplete();

    Integer[] getDataCountPerAngleForWindspeed(int beaufort);

}
