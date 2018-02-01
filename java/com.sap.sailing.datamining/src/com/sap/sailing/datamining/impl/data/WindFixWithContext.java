package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.domain.common.Wind;

public class WindFixWithContext implements HasWindFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasTrackedRaceContext trackedRaceContext;
    
    private final String windSourceType;
    
    private final Wind wind;

    public WindFixWithContext(HasTrackedRaceContext trackedRaceContext, Wind wind, String windSourceType) {
        this.trackedRaceContext = trackedRaceContext;
        this.wind = wind;
        this.windSourceType = windSourceType;
    }
    
    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    @Override
    public Wind getWind() {
        return wind;
    }
    
    @Override
    public double getWindFromDegrees() {
        return getWind().getFrom().getDegrees();
    }

    @Override
    public double getWindSpeedInKnots() {
        return getWind().getKnots();
    }

    @Override
    public String getWindSourceType() {
        return windSourceType;
    }

    @Override
    public String toString() {
        return "WindFixWithContext [trackedRaceContext=" + trackedRaceContext + ", windSourceType=" + windSourceType
                + ", wind=" + wind + "]";
    }
}
