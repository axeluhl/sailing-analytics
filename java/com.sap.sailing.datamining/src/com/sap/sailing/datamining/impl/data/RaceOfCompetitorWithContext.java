package com.sap.sailing.datamining.impl.data;

import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.SailingClusterGroups;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public class RaceOfCompetitorWithContext implements HasRaceOfCompetitorContext {

    private final HasTrackedRaceContext trackedRaceContext;
    private final Competitor competitor;

    public RaceOfCompetitorWithContext(HasTrackedRaceContext trackedRaceContext, Competitor competitor) {
        this.trackedRaceContext = trackedRaceContext;
        this.competitor = competitor;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    private TrackedRace getTrackedRace() {
        return getTrackedRaceContext().getTrackedRace();
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public Tack getTackAtStart() throws NoWindException {
        TimePoint startOfRace = getTrackedRace().getStartOfRace();
        return startOfRace == null ? null : getTrackedRace().getTack(getCompetitor(), startOfRace);
    }
    
    @Override
    public ClusterDTO getPercentageClusterForDistanceToStarboardSideAtStart() {
        Double normalizedDistance = getNormalizedDistanceToStarboardSideAtStart();
        if (normalizedDistance == null) {
            return null;
        }
        
        SailingClusterGroups clusterGroups = Activator.getClusterGroups();
        Cluster<Double> cluster = clusterGroups.getPercentageClusterGroup().getClusterFor(normalizedDistance);
        return new ClusterDTO(clusterGroups.getPercentageClusterFormatter().format(cluster));
    }
    
    @Override
    public Distance getDistanceToStartLineAtStart() {
        return getTrackedRace().getDistanceToStartLine(getCompetitor(), 0);
    }
    
    @Override
    public Double getNormalizedDistanceToStarboardSideAtStart() {
        TrackedRace trackedRace = getTrackedRace();
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, trackedRace.getRace().getCourse().getFirstLeg());
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        if (competitorStartTime == null) {
            return null;
        }
        
        Double distance = trackedRace.getDistanceFromStarboardSideOfStartLine(getCompetitor(), competitorStartTime).getMeters();
        Double length = trackedRace.getStartLine(competitorStartTime).getLength().getMeters();
        return distance / length;
    }
    
    @Override
    public Pair<Double, Double> getNormalizedDistanceToStarboardSideAtStartVsRankAtFirstMark(){
        return new Pair<Double, Double> (getNormalizedDistanceToStarboardSideAtStart(), getRankAtFirstMark());
    }
    
    @Override
    public Distance getWindwardDistanceToAdvantageousLineEndAtStart() {
        TrackedRace trackedRace = getTrackedRace();
        TimePoint startOfRace = trackedRace.getStartOfRace();
        LineDetails startLine = trackedRace.getStartLine(startOfRace);
        Mark advantageousMark = null;
        switch (startLine.getAdvantageousSideWhileApproachingLine()) {
        case PORT:
            advantageousMark = startLine.getPortMarkWhileApproachingLine();
            break;
        case STARBOARD:
            advantageousMark = startLine.getStarboardMarkWhileApproachingLine();
            break;
        }
        if (advantageousMark == null) {
            return null;
        }
        
        GPSFixTrack<Mark, GPSFix> advantageousMarkTrack = trackedRace.getOrCreateTrack(advantageousMark);
        Position advantageousMarkPosition = advantageousMarkTrack.getEstimatedPosition(startOfRace, false);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(getCompetitor());
        Position competitorPosition = competitorTrack.getEstimatedPosition(startOfRace, false);

        TrackedLeg trackedLeg = trackedRace.getTrackedLeg(trackedRace.getRace().getCourse().getFirstLeg());
        Distance distance = trackedLeg.getWindwardDistance(competitorPosition, advantageousMarkPosition, startOfRace, WindPositionMode.LEG_MIDDLE);
        return distance;
    }
    
    @Override
    public Distance getWindwardDistanceToStarboardSideAtStart() {
        TrackedRace trackedRace = getTrackedRace();
        TimePoint startOfRace = trackedRace.getStartOfRace();
        LineDetails startLine = trackedRace.getStartLine(startOfRace);
        Mark starboardMark = startLine.getStarboardMarkWhileApproachingLine();
        if (starboardMark == null) {
            return null;
        }
        
        GPSFixTrack<Mark, GPSFix> starboardMarkTrack = trackedRace.getOrCreateTrack(starboardMark);
        Position advantageousMarkPosition = starboardMarkTrack.getEstimatedPosition(startOfRace, false);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(getCompetitor());
        Position competitorPosition = competitorTrack.getEstimatedPosition(startOfRace, false);

        TrackedLeg trackedLeg = trackedRace.getTrackedLeg(trackedRace.getRace().getCourse().getFirstLeg());
        Distance distance = trackedLeg.getWindwardDistance(competitorPosition, advantageousMarkPosition, startOfRace, WindPositionMode.LEG_MIDDLE);
        return distance;
    }
    
    @Override
    public ClusterDTO getPercentageClusterForRelativeScore() {
        Double relativeScore = getTrackedRaceContext().getRelativeScoreForCompetitor(getCompetitor());
        if (relativeScore == null) {
            return null;
        }
        
        SailingClusterGroups clusterGroups = Activator.getClusterGroups();
        Cluster<Double> cluster = clusterGroups.getPercentageClusterGroup().getClusterFor(relativeScore);
        return new ClusterDTO(clusterGroups.getPercentageClusterFormatter().format(cluster));
    }
    
    @Override
    public Speed getSpeedWhenStarting() {
        return getTrackedRace().getSpeedWhenCrossingStartLine(getCompetitor());
    }
    
    @Override
    public Speed getSpeedTenSecondsBeforeStart() {
        return getTrackedRace().getSpeed(getCompetitor(), TimeUnit.SECONDS.toMillis(10));
    }
    
    @Override
    public Speed getSpeedTenSecondsAfterStart() {
        TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace == null) {
            return null;
        }
        return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(startOfRace.plus(TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Override
    public Double getRankThirtySecondsAfterStart() {
        TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace == null) {
            return null;
        }
        
        int rank = getTrackedRace().getRank(getCompetitor(), startOfRace.plus(TimeUnit.SECONDS.toMillis(30)));
        return rank == 0 ? null : Double.valueOf(rank);
    }
    
    @Override
    public Double getRankAfterHalfOfTheFirstLeg() {
        Course course = getTrackedRace().getRace().getCourse();
        TrackedLegOfCompetitor trackedLeg = getTrackedRace().getTrackedLeg(getCompetitor(), course.getFirstLeg());
        TimePoint startTime = trackedLeg.getStartTime();
        TimePoint finishTime = trackedLeg.getFinishTime();
        if (startTime == null || finishTime == null) {
            return null;
        }
        
        long halfOffset = (finishTime.asMillis() - startTime.asMillis()) / 2;
        int rank = getTrackedRace().getRank(getCompetitor(), startTime.plus(halfOffset));
        return rank == 0 ? null : Double.valueOf(rank);
    }
    
    @Override
    public Double getRankAtFirstMark() {
        Course course = getTrackedRace().getRace().getCourse();
        Waypoint firstMark = course.getFirstLeg().getTo();
        Competitor competitor = getCompetitor();
        final MarkPassing markPassing = getTrackedRace().getMarkPassing(competitor, firstMark);
        int rank = markPassing == null ? 0 : getTrackedRace().getRank(competitor, markPassing.getTimePoint());
        return rank == 0 ? null : Double.valueOf(rank);
    }
    
    @Override
    public Double getRankGainsOrLossesBetweenFirstMarkAndFinish() {
        Double rankAtFirstMark = getRankAtFirstMark();
        Double rankAtFinish = getTrackedRaceContext().getRankAtFinishForCompetitor(getCompetitor());
        return rankAtFirstMark != null && rankAtFinish != null ? rankAtFirstMark - rankAtFinish : null;
    }

    @Override
    public int getNumberOfManeuvers() {
        return getNumberOfTacks() + getNumberOfJibes();
    }

    @Override
    public int getNumberOfTacks() {
        return getNumberOf(ManeuverType.TACK);
    }

    @Override
    public int getNumberOfJibes() {
        return getNumberOf(ManeuverType.JIBE);
    }

    @Override
    public int getNumberOfPenaltyCircles() {
        return getNumberOf(ManeuverType.PENALTY_CIRCLE);
    }

    private int getNumberOf(ManeuverType maneuverType) {
        TrackedRace trackedRace = getTrackedRace();
        int number = 0;
        if (trackedRace != null && trackedRace.getStartOfRace() != null) {
            final TimePoint end;
            final TimePoint endOfTracking = trackedRace.getEndOfTracking();
            if (trackedRace.getEndOfRace() != null) {
                end = trackedRace.getEndOfRace();
            } else {
                final TimePoint now = MillisecondsTimePoint.now();
                if (endOfTracking != null && endOfTracking.before(now)) {
                    end = endOfTracking;
                } else {
                    end = now;
                }
            }
            for (Maneuver maneuver : trackedRace.getManeuvers(getCompetitor(), trackedRace.getStartOfRace(), end, false)) {
                if (maneuver.getType() == maneuverType) {
                    number++;
                }
            }
        }
        return number;
    }

    @Override
    public Distance getDistanceTraveled() {
        return getTrackedRace().getDistanceTraveledIncludingGateStart(getCompetitor(), MillisecondsTimePoint.now());
    }
    
    @Override 
    public Distance getLineLengthAtStart() {
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, getTrackedRace().getRace().getCourse().getFirstLeg());
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        if (competitorStartTime == null) {
            return null;
        }
        return getTrackedRace().getStartLine(competitorStartTime).getLength();
    }
    
}