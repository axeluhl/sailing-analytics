package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Map;

public interface PolarSheetsData extends Serializable {
    
    Number[][] getAveragedPolarDataByWindSpeed();
    
    int getDataCount();

    Integer[] getDataCountPerAngleForWindspeed(int windIndex);

    WindSpeedStepping getStepping();

    /**
     * @return key is a zero-based index into the wind speed range list that uses the {@link #getStepping() stepping
     *         configured}; value is a map whose keys are true wind angles in degrees in the range 0 &lt;= angle &lt;
     *         360 and whose values are all the data points collected for this
     * 
     */
    Map<Integer, Map<Integer, PolarSheetsHistogramData>> getHistogramDataMap();

}
