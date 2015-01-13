package com.sap.sailing.polars.data;

public interface DataPointWithOriginInfo extends Comparable<DataPointWithOriginInfo> {

    public abstract Double getRawData();

    public abstract String getWindGaugeIdString();

    public abstract String getDayString();

}