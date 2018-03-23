package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;

public class GPSFixWithContext implements HasGPSFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    
    private final GPSFixMoving gpsFix;
    private Wind wind;

    public GPSFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, GPSFixMoving gpsFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.gpsFix = gpsFix;
    }
    
    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitorContext;
    }

    @Override
    public GPSFixMoving getGPSFix() {
        return gpsFix;
    }

    @Override
    public Wind getWindInternal() {
        return wind;
    }

    @Override
    public void setWindInternal(Wind wind) {
        this.wind = wind;
    }
    
    @Override
    public Bearing getTrueWindAngle() throws NoWindException {
        return getTrackedRace().getTWA(getTrackedLegOfCompetitorContext().getCompetitor(), getTimePoint());
    }

    @Override
    public Bearing getAbsoluteTrueWindAngle() throws NoWindException {
        return getTrackedRace().getTWA(getTrackedLegOfCompetitorContext().getCompetitor(), getTimePoint()).abs();
    }
}