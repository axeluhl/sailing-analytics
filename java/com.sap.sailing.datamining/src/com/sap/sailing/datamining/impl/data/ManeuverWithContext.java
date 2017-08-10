package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;

public class ManeuverWithContext implements HasManeuverContext {
    private static final long serialVersionUID = 7717196485074392156L;
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitor;
    private final Maneuver maneuver;
    private Wind wind;

    public ManeuverWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitor, Maneuver maneuver) {
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.maneuver = maneuver;
    }

    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitor;
    }

    @Override
    public Maneuver getManeuver() {
        return maneuver;
    }

    @Override
    public ManeuverType getManeuverType() {
        return getManeuver().getType();
    }

    @Override
    public NauticalSide getToSide() {
        return getManeuver().getDirectionChangeInDegrees() >= 0 ? NauticalSide.STARBOARD : NauticalSide.PORT;
    }

    @Override
    public Double getAbsoluteDirectionChangeInDegrees() {
        return Math.abs(getManeuver().getDirectionChangeInDegrees());
    }
    
    @Override
    public Distance getManeuverLoss() {
        return getManeuver().getManeuverLoss();
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
    public Double getEnteringCoG() {
        return getCoGAtTimepoint(maneuver.getTimePointBefore());
    }

    @Override
    public Double getExitingCoG() {
        return getCoGAtTimepoint(maneuver.getTimePointAfter());
    }
    
    private Double getCoGAtTimepoint(TimePoint timepoint) {
        Wind wind = trackedLegOfCompetitor.getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getWind(maneuver.getPosition(), timepoint);
        GPSFixTrack<Competitor,GPSFixMoving> competitorTrack = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace().getTrack(getTrackedLegOfCompetitorContext().getCompetitor());
        if(wind != null) {
            competitorTrack.lockForRead();
            try {
                SpeedWithBearing speedWithBearing = competitorTrack.getEstimatedSpeed(timepoint);
                double beatAngle =  wind.getFrom().getDifferenceTo(speedWithBearing.getBearing()).getDegrees();
                if(beatAngle < 0) {
                    beatAngle += 360;
                }
                return beatAngle;
            } finally {
                competitorTrack.unlockAfterRead();
            }
        }
        return null;
    }
}
