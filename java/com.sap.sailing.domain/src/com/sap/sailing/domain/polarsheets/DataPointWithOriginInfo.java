package com.sap.sailing.domain.polarsheets;

public class DataPointWithOriginInfo implements Comparable<DataPointWithOriginInfo>{

    private Double rawData;
    
    private String windGaugeIdString;

    public DataPointWithOriginInfo(Double rawData, String windGaugeIdString) {
        this.rawData = rawData;
        this.windGaugeIdString = windGaugeIdString;
    }

    public Double getRawData() {
        return rawData;
    }

    public String getWindGaugeIdString() {
        return windGaugeIdString;
    }

    @Override
    public int compareTo(DataPointWithOriginInfo o) {
        return getRawData().compareTo(o.getRawData());
    }
       
}
