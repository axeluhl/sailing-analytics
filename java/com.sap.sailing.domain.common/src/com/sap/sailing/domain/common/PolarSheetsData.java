package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Map;

public interface PolarSheetsData extends Serializable {
    
    Number[][] getAveragedPolarDataByWindSpeed();
    
    int getDataCount();

    Integer[] getDataCountPerAngleForWindspeed(int windIndex);

    WindStepping getStepping();

    Map<Integer, Map<Integer,PolarSheetsHistogramData>> getHistogramDataMap();

}
