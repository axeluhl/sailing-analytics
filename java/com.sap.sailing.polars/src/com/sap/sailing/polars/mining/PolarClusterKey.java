package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface PolarClusterKey {

    /**
     * 
     * @return degrees boat to windDirection in DEG
     */
    @Dimension(messageKey = "angleToWind")
    RoundedAngleToTheWind getRoundedAngleToTheWind();

    /**
     * 
     * @return wind speed level
     */
    @Dimension(messageKey = "windSpeed")
    WindSpeedLevel getWindSpeedLevel();

    @Dimension(messageKey = "boatClass")
    BoatClass getBoatClass();

}