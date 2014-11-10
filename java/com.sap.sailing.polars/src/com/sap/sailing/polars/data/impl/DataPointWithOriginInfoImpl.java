package com.sap.sailing.polars.data.impl;

import com.sap.sailing.polars.data.DataPointWithOriginInfo;

public class DataPointWithOriginInfoImpl implements DataPointWithOriginInfo {

    private Double rawData;
    
    private String windGaugeIdString;

    private String dayString;

    public DataPointWithOriginInfoImpl(Double rawData, String windGaugeIdString, String dayString) {
        this.rawData = rawData;
        this.windGaugeIdString = windGaugeIdString;
        this.dayString = dayString;
    }

    @Override
    public Double getRawData() {
        return rawData;
    }

    @Override
    public String getWindGaugeIdString() {
        return windGaugeIdString;
    }
    
    @Override
    public String getDayString() {
        return dayString;
    }

    @Override
    public int compareTo(DataPointWithOriginInfo o) {
        return getRawData().compareTo(o.getRawData());
    }
       
}
