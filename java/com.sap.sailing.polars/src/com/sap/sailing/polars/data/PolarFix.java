package com.sap.sailing.polars.data;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;

public interface PolarFix {

    public abstract SpeedWithBearing getBoatSpeed();

    public abstract Speed getWindSpeed();

    public abstract double getTrueWindAngle();

    public abstract String getGaugeIdString();

    public abstract String getDayString();

}