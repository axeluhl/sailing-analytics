package com.sap.sailing.polars.mining;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface PolarClusterKey {

    /**
     * 
     * @return degrees boat to windDirection in DEG
     */
    @Dimension(messageKey = "angleToWind")
    public abstract RoundedAngleToTheWind getRoundedAngleToTheWind();

    /**
     * 
     * @return wind speed level
     */
    @Dimension(messageKey = "windSpeed")
    public abstract WindSpeedLevel getWindSpeedLevel();

}