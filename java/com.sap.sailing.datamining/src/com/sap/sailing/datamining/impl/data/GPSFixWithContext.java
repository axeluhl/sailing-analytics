package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;

public class GPSFixWithContext implements HasGPSFixContext {
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    private final GPSFixMoving gpsFix;

    public GPSFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, GPSFixMoving gpsFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.gpsFix = gpsFix;
    }
    
    private TimePoint getTimePoint() {
        return getGPSFix().getTimePoint();
    }
    
    private TrackedRace getTrackedRace() {
        return getTrackedLegOfCompetitorContext().getTrackedRace();
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
    public Bearing getTrueWindAngle() throws NoWindException {
        return getTrackedRace().getTWA(getTrackedLegOfCompetitorContext().getCompetitor(), getTimePoint());
    }

    @Override
    public Bearing getAbsoluteTrueWindAngle() throws NoWindException {
        return getTrackedRace().getTWA(getTrackedLegOfCompetitorContext().getCompetitor(), getTimePoint()).abs();
    }
}