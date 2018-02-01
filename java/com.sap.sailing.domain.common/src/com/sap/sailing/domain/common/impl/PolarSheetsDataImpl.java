package com.sap.sailing.domain.common.impl;

import java.util.Map;

import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.domain.common.WindSpeedStepping;

public class PolarSheetsDataImpl implements PolarSheetsData {

    private static final long serialVersionUID = -4649254807341866894L;

    private Number[][] averagedPolarDataByWindSpeed;
    
    private int dataCount;
    
    private WindSpeedStepping stepping;

    private Map<Integer,Integer[]> dataCountPerAngleForWindspeed;

    private Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap;

    //For GWT Serialization
    PolarSheetsDataImpl() {};
   
    public PolarSheetsDataImpl(Number[][] averagedPolarDataByWindSpeed, int dataCount,
            Map<Integer, Integer[]> dataCountPerAngleForWindspeed, WindSpeedStepping stepping,
            Map<Integer, Map<Integer, PolarSheetsHistogramData>> histogramDataMap) {
        this.averagedPolarDataByWindSpeed = averagedPolarDataByWindSpeed;
        this.dataCount = dataCount;
        this.dataCountPerAngleForWindspeed = dataCountPerAngleForWindspeed;
        this.stepping = stepping;
        this.histogramDataMap = histogramDataMap;
    }


    @Override
    public Number[][] getAveragedPolarDataByWindSpeed() {
        return averagedPolarDataByWindSpeed;
    }

    @Override
    public int getDataCount() {
        return dataCount;
    }


    @Override
    public Integer[] getDataCountPerAngleForWindspeed(int beaufort) {
        return dataCountPerAngleForWindspeed.get(beaufort);
    }
    
    @Override
    public WindSpeedStepping getStepping() {
        return stepping;
    }
    
    @Override
    public Map<Integer, Map<Integer,PolarSheetsHistogramData>> getHistogramDataMap() {
        return histogramDataMap;
    }

}
