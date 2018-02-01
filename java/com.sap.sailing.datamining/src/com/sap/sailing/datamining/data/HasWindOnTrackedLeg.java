package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Wind;

public interface HasWindOnTrackedLeg extends HasTrackedLegOfCompetitor, HasWind {
    default Wind getWind() {
        if (getWindInternal() == null) {
            setWindInternal(getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace()
                    .getWind(getPosition(), getTimePoint()));
        }
        return getWindInternal();
    }
    
    Wind getWindInternal();
    
    void setWindInternal(Wind wind);
}
