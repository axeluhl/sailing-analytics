package com.sap.sailing.domain.tracking;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public interface WindSummary {
    /**
     * The "from" direction of the wind
     */
    Bearing getTrueWindDirection();

    Speed getTrueLowerboundWind();

    Speed getTrueUpperboundWind();

}
