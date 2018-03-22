package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasBravoFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.WindPositionMode;

public class BravoFixWithContext implements HasBravoFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    
    private final BravoFix bravoFix;
    private Wind wind;

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
    
    @Override
    public Wind getWindInternal() {
        return wind;
    }

    @Override
    public void setWindInternal(Wind wind) {
        this.wind = wind;
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
        return getTrackedRace().getWind(getGpsFixTrack().getEstimatedPosition(getTimePoint(), /* extrapolate */ true), getTimePoint());
    }

    @Override
    public Bearing getAbsoluteTrueWindAngle() {
        return getTrackedRace().getTWA(getCompetitor(), getTimePoint()).abs();
    }

    private Competitor getCompetitor() {
        return getTrackedLegOfCompetitorContext().getCompetitor();
    }

    @Override
    public Position getPosition() {
        return getGpsFixTrack().getEstimatedPosition(getTimePoint(), /* extrapolate */ true);
    }

    private GPSFixTrack<Competitor, GPSFixMoving> getGpsFixTrack() {
        return getTrackedRace().getTrack(getTrackedLegOfCompetitorContext().getCompetitor());
    }
}
