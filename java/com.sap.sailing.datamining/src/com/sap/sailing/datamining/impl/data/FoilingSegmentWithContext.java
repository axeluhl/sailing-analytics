package com.sap.sailing.datamining.impl.data;

import java.util.Locale;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.data.HasBravoFixTrackContext;
import com.sap.sailing.datamining.data.HasFoilingSegmentContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class FoilingSegmentWithContext implements HasFoilingSegmentContext {
    private final HasBravoFixTrackContext bravoFixTrackContext;
    private final TimePoint startOfFoilingSegment;
    private final TimePoint endOfFoilingSegment;
    
    public FoilingSegmentWithContext(HasBravoFixTrackContext bravoFixTrackContext, TimePoint startOfFoilingSegment,
            TimePoint endOfFoilingSegment) {
        super();
        this.bravoFixTrackContext = bravoFixTrackContext;
        this.startOfFoilingSegment = startOfFoilingSegment;
        this.endOfFoilingSegment = endOfFoilingSegment;
    }

    @Override
    public HasBravoFixTrackContext getBravoFixTrackContext() {
        return bravoFixTrackContext;
    }

    @Override
    public TimePoint getStartOfFoilingSegment() {
        return startOfFoilingSegment;
    }

    @Override
    public TimePoint getEndOfFoilingSegment() {
        return endOfFoilingSegment;
    }

    @Override
    public Duration getDuration() {
        return getStartOfFoilingSegment().until(getEndOfFoilingSegment());
    }

    @Override
    public Distance getDistance() {
        return getGpsFixTrack().
                getDistanceTraveled(getStartOfFoilingSegment(), getEndOfFoilingSegment());
    }

    private GPSFixTrack<Competitor, GPSFixMoving> getGpsFixTrack() {
        return getTrackedRace().getTrack(getCompetitor());
    }

    private Competitor getCompetitor() {
        return getBravoFixTrackContext().getRaceOfCompetitorContext().getCompetitor();
    }

    private TrackedRace getTrackedRace() {
        return getBravoFixTrackContext().getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace();
    }

    @Override
    public Double getTakeoffSpeedInKnots() {
        final SpeedWithBearing estimatedSpeed = getGpsFixTrack().getEstimatedSpeed(getStartOfFoilingSegment());
        return estimatedSpeed==null?null:estimatedSpeed.getKnots();
    }

    @Override
    public Double getLandingSpeedInKnots() {
        final SpeedWithBearing estimatedSpeed = getGpsFixTrack().getEstimatedSpeed(getEndOfFoilingSegment());
        return estimatedSpeed==null?null:estimatedSpeed.getKnots();
    }

    @Override
    public Bearing getTrueWindAngleAtTakeoffInDegrees() throws NoWindException {
        return getTrackedRace().getTrackedLeg(getCompetitor(), getStartOfFoilingSegment()).getBeatAngle(getStartOfFoilingSegment());
    }

    @Override
    public Bearing getTrueWindAngleAtLandingInDegrees() throws NoWindException {
        return getTrackedRace().getTrackedLeg(getCompetitor(), getEndOfFoilingSegment()).getBeatAngle(getEndOfFoilingSegment());
    }

    @Override
    public ClusterDTO getWindStrengthAsBeaufortClusterAtTakeoff(Locale locale,
            ResourceBundleStringMessages stringMessages) {
        return getWindStrengthAsBeaufortCluster(locale, stringMessages, getWindAtTakeoff());
    }

    @Override
    public ClusterDTO getWindStrengthAsBeaufortClusterAtLanding(Locale locale,
            ResourceBundleStringMessages stringMessages) {
        return getWindStrengthAsBeaufortCluster(locale, stringMessages, getWindAtLanding());
     }

    private Wind getWind(TimePoint timePoint) {
        return getTrackedRace().getWind(getGpsFixTrack().getEstimatedPosition(timePoint, /* extrapolate */ true), timePoint);
    }

    @Override
    public Wind getWindAtTakeoff() {
        return getWind(getStartOfFoilingSegment());
    }

    @Override
    public Wind getWindAtLanding() {
        return getWind(getEndOfFoilingSegment());
    }

    private ClusterDTO getWindStrengthAsBeaufortCluster(Locale locale, ResourceBundleStringMessages stringMessages, Wind wind) {
        Cluster<?> cluster = Activator.getClusterGroups().getWindStrengthInBeaufortClusterGroup().getClusterFor(wind);
        return new ClusterDTO(cluster.asLocalizedString(locale, stringMessages));
    }

}
