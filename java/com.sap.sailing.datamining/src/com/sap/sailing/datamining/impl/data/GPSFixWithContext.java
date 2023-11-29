package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TackType;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.NoCachingWindLegTypeAndLegBearingCache;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * Equality is based on the {@link #getGPSFix() GPS fix} only.
 */
public class GPSFixWithContext implements HasGPSFixContext {
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    private final GPSFixMoving gpsFix;

    public GPSFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, GPSFixMoving gpsFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.gpsFix = gpsFix;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gpsFix == null) ? 0 : gpsFix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GPSFixWithContext other = (GPSFixWithContext) obj;
        if (gpsFix == null) {
            if (other.gpsFix != null)
                return false;
        } else if (!gpsFix.equals(other.gpsFix))
            return false;
        return true;
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

    @Override
    public SpeedWithBearing getVelocityMadeGood() {
        return getTrackedRace().getVelocityMadeGood(getTrackedLegOfCompetitorContext().getCompetitor(), getTimePoint());
    }

    @Override
    public Distance getXTE() {
        return getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getSignedCrossTrackError(getTimePoint());
    }

    @Override
    public Distance getAbsoluteXTE() {
        return getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getAbsoluteCrossTrackError(getTimePoint());
    }
    
    @Override
    public TackType getTackType() throws NoWindException {
        return getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getTackType(getTimePoint());
    }
}