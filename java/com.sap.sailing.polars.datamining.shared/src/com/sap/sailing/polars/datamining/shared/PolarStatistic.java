package com.sap.sailing.polars.datamining.shared;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Speed;

public interface PolarStatistic {

    public SpeedWithBearing getBoatSpeed();

    public Speed getWindSpeed();

    public double getTrueWindAngleDeg();
    
    public PolarDataMiningSettings getSettings();

}
