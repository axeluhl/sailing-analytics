package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.domain.common.Wind;

public class WindFixWithContext implements HasWindFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasWindTrackContext windTrackContext;
    
    private final String windSourceType;
    
    private final Wind wind;

    public WindFixWithContext(HasWindTrackContext windTrackContext, Wind wind, String windSourceType) {
        this.windTrackContext = windTrackContext;
        this.wind = wind;
        this.windSourceType = windSourceType;
    }
    
    @Override
    public HasWindTrackContext getWindTrackContext() {
        return windTrackContext;
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
        return "WindFixWithContext [trackedRaceContext=" + windTrackContext + ", windSourceType=" + windSourceType
                + ", wind=" + wind + "]";
    }
}
