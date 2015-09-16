package com.sap.sailing.polars.datamining.shared;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;

public interface PolarStatistic {

    public SpeedWithBearing getBoatSpeed();

    public Speed getWindSpeed();

    public double getTrueWindAngleDeg();
    
    public PolarSheetGenerationSettings getSettings();

}
