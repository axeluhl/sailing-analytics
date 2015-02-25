package com.sap.sailing.dashboards.gwt.client.windchart.compass;

public interface LocationPointerCompassAngleDistanceListener {
    
    public void angleChanged(double angle);
    public void angleAndDistanceChanged(double angle, double distance);
    public void setAngleOffset(double offset);
}
