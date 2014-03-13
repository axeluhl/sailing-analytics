package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Map;

public interface PolarSheetsData extends Serializable {
    
    Number[][] getAveragedPolarDataByWindSpeed();
    
    int getDataCount();
    
    boolean isComplete();

    Integer[] getDataCountPerAngleForWindspeed(int beaufort);

    WindStepping getStepping();

    Map<Integer, Map<Integer,PolarSheetsHistogramData>> getHistogramDataMap();

}
