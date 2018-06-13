package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasBravoFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class BravoFixWithContext implements HasBravoFixContext {
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    
    private final BravoFix bravoFix;

    public BravoFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, BravoFix bravoFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.bravoFix = bravoFix;
    }
    
    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitorContext;
    }

    @Override
    public BravoFix getBravoFix() {
        return bravoFix;
    }
    
    private TimePoint getTimePoint() {
        return getBravoFix().getTimePoint();
    }
    
    private TrackedRace getTrackedRace() {
        return getTrackedLegOfCompetitorContext().getTrackedRace();
    }
    
    @Override
    public SpeedWithBearing getSpeed() {
        return getGpsFixTrack().getEstimatedSpeed(getTimePoint());
    }

    @Override
    public Speed getVelocityMadeGood() {
        return getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getVelocityMadeGood(getTimePoint(), WindPositionMode.EXACT);
    }

    @Override
    public Wind getWind() {
        return HasBravoFixContext.super.getWind();
    }

    @Override
    public Bearing getTrueWindAngle() {
        return getTrackedRace().getTWA(getCompetitor(), getTimePoint());
    }

    @Override
    public Bearing getAbsoluteTrueWindAngle() {
        return getTrackedRace().getTWA(getCompetitor(), getTimePoint()).abs();
    }

    private Competitor getCompetitor() {
        return getTrackedLegOfCompetitorContext().getCompetitor();
    }

    private GPSFixTrack<Competitor, GPSFixMoving> getGpsFixTrack() {
        return getTrackedRace().getTrack(getTrackedLegOfCompetitorContext().getCompetitor());
    }
}
