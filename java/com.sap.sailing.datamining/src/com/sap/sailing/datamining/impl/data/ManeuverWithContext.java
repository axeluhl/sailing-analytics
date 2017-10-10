package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
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

    public Double getManeuverEnteringSpeed() {
        Competitor competitor = getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getCompetitor();
        TrackedRace trackedRace = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext()
                .getTrackedRace();
        return trackedRace.getTrack(competitor).getEstimatedSpeed(maneuver.getTimePointBefore()).getKnots();
    }

    public Double getManeuverExitingSpeed() {
        Competitor competitor = getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getCompetitor();
        TrackedRace trackedRace = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext()
                .getTrackedRace();
        return trackedRace.getTrack(competitor).getEstimatedSpeed(maneuver.getTimePointAfter()).getKnots();
    }

    @Override
    public Double getManeuverDuration() {
        return maneuver.getTimePointBefore().until(maneuver.getTimePointAfter()).asSeconds();
    }

    @Override
    public Double getAbsTWAAtManeuverClimax() {
        Competitor competitor = getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getCompetitor();
        TrackedRace trackedRace = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext()
                .getTrackedRace();
        Wind wind = trackedRace.getWind(maneuver.getPosition(), maneuver.getTimePoint());
        SpeedWithBearing speedWithBearing = trackedRace.getTrack(competitor).getEstimatedSpeed(maneuver.getTimePoint());
        return Math.abs(wind.getFrom().getDifferenceTo(speedWithBearing.getBearing()).getDegrees());
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
    public Double getEnteringAbsTWA() {
        return getAbsTWAAtTimepoint(maneuver.getTimePointBefore());
    }

    @Override
    public Double getExitingAbsTWA() {
        return getAbsTWAAtTimepoint(maneuver.getTimePointAfter());
    }

    private Double getAbsTWAAtTimepoint(TimePoint timepoint) {
        Double twa = getTWAAtTimepoint(timepoint);
        if(twa == null) {
            return null;
        }
        return Math.abs(twa);
    }
    
    private Double getTWAAtTimepoint(TimePoint timepoint) {
        Wind wind = trackedLegOfCompetitor.getTrackedLegContext().getTrackedRaceContext().getTrackedRace()
                .getWind(maneuver.getPosition(), timepoint);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrackedLegOfCompetitorContext()
                .getTrackedLegContext().getTrackedRaceContext().getTrackedRace()
                .getTrack(getTrackedLegOfCompetitorContext().getCompetitor());
        if (wind != null) {
            competitorTrack.lockForRead();
            try {
                SpeedWithBearing speedWithBearing = competitorTrack.getEstimatedSpeed(timepoint);
                double twa = wind.getFrom().getDifferenceTo(speedWithBearing.getBearing()).getDegrees();
                return twa;
            } finally {
                competitorTrack.unlockAfterRead();
            }
        }
        return null;
    }

    @Override
    public Double getEnteringManeuverSpeedMinusExitingSpeed() {
        return getManeuverEnteringSpeed() - getManeuverExitingSpeed();
    }

    @Override
    public Double getRatioBetweenManeuverEnteringAndExitingSpeed() {
        return getManeuverEnteringSpeed() / getManeuverExitingSpeed();
    }

    @Override
    public Tack getTackBeforeManeuver() {
        Double twa = getTWAAtTimepoint(maneuver.getTimePointBefore());
        if(twa == null) {
            return null;
        }
        if(twa < 0) {
            return Tack.PORT;
        }
        return Tack.STARBOARD;
    }
}
