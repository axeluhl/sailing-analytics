package com.sap.sailing.domain.polarsheets;

public class DataPointWithOriginInfo implements Comparable<DataPointWithOriginInfo>{

    private Double rawData;
    
    private String windGaugeIdString;

    private String dayString;

    public DataPointWithOriginInfo(Double rawData, String windGaugeIdString, String dayString) {
        this.rawData = rawData;
        this.windGaugeIdString = windGaugeIdString;
        this.dayString = dayString;
    }

    public Double getRawData() {
        return rawData;
    }

    public String getWindGaugeIdString() {
        return windGaugeIdString;
    }
    
    public String getDayString() {
        return dayString;
    }

    @Override
    public int compareTo(DataPointWithOriginInfo o) {
        return getRawData().compareTo(o.getRawData());
    }
       
}
