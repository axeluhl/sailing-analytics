package com.sap.sailing.domain.common.impl;

import java.util.Map;

import com.sap.sailing.domain.common.PolarSheetsData;

public class PolarSheetsDataImpl implements PolarSheetsData {

    private static final long serialVersionUID = -4649254807341866894L;

    private Number[][] averagedPolarDataByWindSpeed;
    
    private boolean complete;
    
    private int dataCount;
    
    private Integer[] stepping;

    private Map<Integer,Integer[]> dataCountPerAngleForWindspeed;
    
    //For GWT Serialization
    PolarSheetsDataImpl() {};
   
    public PolarSheetsDataImpl(Number[][] averagedPolarDataByWindSpeed, boolean complete, int dataCount, Map<Integer,Integer[]> dataCountPerAngleForWindspeed, Integer[] stepping) {
        this.averagedPolarDataByWindSpeed = averagedPolarDataByWindSpeed;
        this.complete = complete;
        this.dataCount = dataCount;
        this.dataCountPerAngleForWindspeed = dataCountPerAngleForWindspeed;
        this.stepping = stepping;
    }

    @Override
    public Number[][] getAveragedPolarDataByWindSpeed() {
        return averagedPolarDataByWindSpeed;
    }

    @Override
    public boolean isComplete() {
        return complete;
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
    public Integer[] getStepping() {
        return stepping;
    }

}
