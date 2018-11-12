package com.sap.sailing.datamining.impl.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.SailingClusterGroups;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
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
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
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
    public String getSailID() {
        Boat boatOfCompetitor = getTrackedRace().getBoatOfCompetitor(getCompetitor());
        return boatOfCompetitor != null ? boatOfCompetitor.getSailID() : null;
    }
    
    @Override
    public ClusterDTO getPercentageClusterForDistanceToStarboardSideAtStart() {
        Double normalizedDistance = getNormalizedDistanceToStarboardSideAtStartOfCompetitor();
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
    public Double getNormalizedDistanceToStarboardSideAtStartOfCompetitor() {
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
    public Pair<Double, Integer> getNormalizedDistanceToStarboardSideAtStartOfCompetitorVsRankAtFirstMark(){
        return new Pair<>(getNormalizedDistanceToStarboardSideAtStartOfCompetitor(), getRankAtFirstMark());
    }
    
    public Distance getWindwardDistanceToAdvantageousLineEndAtStartOf(TimePoint timepoint) {
        if(timepoint == null) {
            return null;
        }
        TrackedRace trackedRace = getTrackedRace();
        LineDetails startLine = trackedRace.getStartLine(timepoint);
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
        Position advantageousMarkPosition = advantageousMarkTrack.getEstimatedPosition(timepoint, false);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(getCompetitor());
        Position competitorPosition = competitorTrack.getEstimatedPosition(timepoint, false);

        TrackedLeg trackedLeg = trackedRace.getTrackedLeg(trackedRace.getRace().getCourse().getFirstLeg());
        Distance distance = trackedLeg.getWindwardDistance(competitorPosition, advantageousMarkPosition, timepoint, WindPositionMode.LEG_MIDDLE);
        return distance;
    }
    
    @Override
    public Distance getWindwardDistanceToAdvantageousLineEndAtStartofRace() {
        return getWindwardDistanceToAdvantageousLineEndAtStartOf(getTrackedRace().getStartOfRace());
    }
    
    @Override
    public Distance getWindwardDistanceToAdvantageousLineEndAtStartofCompetitor() {
        return getWindwardDistanceToAdvantageousLineEndAtStartOf(getTrackedRace().getTrackedLeg(getCompetitor(), getTrackedRace().getRace().getCourse().getFirstLeg()).getStartTime());
    }
    
    @Override
    public Distance getAbsoluteWindwardDistanceToStarboardSideAtStartOfCompetitor() {
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = getFirstLegOfCompetitor();
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        if(competitorStartTime == null) {
            return null;
        }
        
        TrackedRace trackedRace = getTrackedRace();
        TimePoint startOfRace = trackedRace.getStartOfRace();
        LineDetails startLine = trackedRace.getStartLine(startOfRace);
        Mark starboardMark = startLine.getStarboardMarkWhileApproachingLine();
        if (starboardMark == null) {
            return null;
        }
        
        GPSFixTrack<Mark, GPSFix> starboardMarkTrack = trackedRace.getOrCreateTrack(starboardMark);
        Position starboardMarkPosition = starboardMarkTrack.getEstimatedPosition(startOfRace, false);
        GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(getCompetitor());
        Position competitorPosition = competitorTrack.getEstimatedPosition(startOfRace, false);

        TrackedLeg trackedLeg = trackedRace.getTrackedLeg(trackedRace.getRace().getCourse().getFirstLeg());
        Distance distance = trackedLeg.getAbsoluteWindwardDistance(competitorPosition, starboardMarkPosition, startOfRace, WindPositionMode.LEG_MIDDLE);
        
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
    public Speed getSpeedTenSecondsAfterStartOfRace() {
        TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace == null) {
            return null;
        }
        return getTrackOfCompetitor().getEstimatedSpeed(startOfRace.plus(TimeUnit.SECONDS.toMillis(10)));
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
    public Integer getRankAtFirstMark() {
        Course course = getTrackedRace().getRace().getCourse();
        Waypoint firstMark = course.getFirstLeg().getTo();
        Competitor competitor = getCompetitor();
        final MarkPassing markPassing = getTrackedRace().getMarkPassing(competitor, firstMark);
        int rank = markPassing == null ? 0 : getTrackedRace().getRank(competitor, markPassing.getTimePoint());
        return rank == 0 ? null : rank;
    }
    
    @Override
    public Integer getRankGainsOrLossesBetweenFirstMarkAndFinish() {
        Integer rankAtFirstMark = getRankAtFirstMark();
        Integer rankAtFinish = getTrackedRaceContext().getRankAtFinishForCompetitor(getCompetitor());
        return rankAtFirstMark != null && rankAtFinish != null ? rankAtFirstMark - rankAtFinish : null;
    }

    @Override
    public int getNumberOfManeuvers() {
        Set<ManeuverType> maneuverTypes = new HashSet<>();
        maneuverTypes.add(ManeuverType.TACK);
        maneuverTypes.add(ManeuverType.JIBE);
        return getNumberOf(maneuverTypes);
    }

    @Override
    public int getNumberOfTacks() {
        return getNumberOf(Collections.singleton(ManeuverType.TACK));
    }

    @Override
    public int getNumberOfJibes() {
        return getNumberOf(Collections.singleton(ManeuverType.JIBE));
    }

    @Override
    public int getNumberOfPenaltyCircles() {
        return getNumberOf(Collections.singleton(ManeuverType.PENALTY_CIRCLE));
    }

    private int getNumberOf(Set<ManeuverType> maneuverTypes) {
        int number = 0;
        TrackedRace trackedRace = getTrackedRace();
        if (trackedRace != null) {
            TimePoint from = null;
            TimePoint to = null;
            Competitor competitor = getCompetitor();
            Course course = trackedRace.getRace().getCourse();
            List<Waypoint> waypoints = Util.createList(course.getWaypoints());
            
            int fromIndex = 0;
            while (fromIndex < waypoints.size()) {
                MarkPassing markPassing = trackedRace.getMarkPassing(competitor, waypoints.get(fromIndex));
                TimePoint passingTime = markPassing != null ? markPassing.getTimePoint() : null;
                if (passingTime != null) {
                    if (from == null) {
                        from = passingTime;
                    } else {
                        to = passingTime;
                    }
                }
                fromIndex++;
            }
            
            if (from != null && to != null) {
                for (Maneuver maneuver : trackedRace.getManeuvers(getCompetitor(), from, to, false)) {
                    if (maneuverTypes.contains(maneuver.getType())) {
                        number++;
                    }
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
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = getFirstLegOfCompetitor();
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        if (competitorStartTime == null) {
            return null;
        }
        return getTrackedRace().getStartLine(competitorStartTime).getLength();
    }
    
    @Override
    public Pair<Double, Integer> getRelativeDistanceToStarboardSideAtStartOfCompetitorVsFinalRank(){
        return new Pair<>(getNormalizedDistanceToStarboardSideAtStartOfCompetitor(), getTrackedRaceContext().getRankAtFinishForCompetitor(getCompetitor()));
    }
    
    @Override
    public Pair<Double, Double> getWindwardDistanceToAdvantageousEndOfLineAtStartOfRaceVsRelativeDistanceToAdvantageousEndOfLineAtStartOfRace(){
        return new Pair<>(getWindwardDistanceToAdvantageousLineEndAtStartofRace().getMeters(), getRelativeDistanceToAdvantageousEndOfLineAtStartOfRace());
    }
    
    @Override
    public Double getRelativeDistanceToAdvantageousEndOfLineAtStartOfRace() {
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
        
        Double distance = competitorPosition.getDistance(advantageousMarkPosition).getMeters();
        
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = getTrackedRace().getTrackedLeg(competitor, trackedRace.getRace().getCourse().getFirstLeg());
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        Double length = trackedRace.getStartLine(competitorStartTime).getLength().getMeters();
        return distance / length;
    }
    
    @Override
    public Duration getDuration() {
        Duration duration = null;
        TrackedRace race = getTrackedRace();
        Course course = race.getRace().getCourse();
        MarkPassing startPassing = race.getMarkPassing(competitor, course.getFirstWaypoint());
        MarkPassing finishPassing = race.getMarkPassing(competitor, course.getLastWaypoint());
        if (startPassing != null && finishPassing != null) {
            long durationMillis = finishPassing.getTimePoint().asMillis() - startPassing.getTimePoint().asMillis();
            duration = new MillisecondsDurationImpl(durationMillis);
        }
        return duration;
    }

    @Override
    public Double getRelativeDistanceToStarboardSideAtStartOfRace() {
        TrackedRace trackedRace = getTrackedRace();
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, trackedRace.getRace().getCourse().getFirstLeg());
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        if (competitorStartTime == null) {
            return null;
        }

        return getNormalizeDistanceToStarboardSideAtTimePoint(getStartOfRace());
    }

    @Override
    public Speed getVMG5SecondsBeforeStartOfRace() {
        return getTrackedRace().getVelocityMadeGood(getCompetitor(), getStartOfRace().minus(TimeUnit.SECONDS.toMillis(5)));
    }

    @Override
    public Speed getVMGAtStartOfRace() {
        return getTrackedRace().getVelocityMadeGood(getCompetitor(), getStartOfRace());
    }

    @Override
    public Speed getVMG5SecondsAfterStartOfRace() {
        return getTrackedRace().getVelocityMadeGood(getCompetitor(), getStartOfRace().plus(TimeUnit.SECONDS.toMillis(5)));
    }

    @Override
    public Pair<Double, Integer> getRelativeDistanceToAdvantageousSideAtStartOfRaceVsRankAtFirstMark() {
        return new Pair<>(getRelativeDistanceToAdvantageousEndOfLineAtStartOfRace(), getRankAtFirstMark());
    }

    @Override
    public Pair<Integer, Integer> getRankAtFirstMarkVsFinalRank() {
        return new Pair<>(getRankAtFirstMark(), getFinalRank());
    }

    @Override
    public Integer getRankThirtySecondsAfterStartOfRace() {
        return getRankAt(getStartOfRace().plus(TimeUnit.SECONDS.toMillis(30)));
    }
    
    @Override
    public Integer getRankSixtySecondsAfterStartOfRace() {
        return getRankAt(getStartOfRace().plus(TimeUnit.SECONDS.toMillis(60)));
    }
    
    @Override
    public Integer getRankNinetySecondsAfterStartOfRace() {
        return getRankAt(getStartOfRace().plus(TimeUnit.SECONDS.toMillis(90)));
    }

    @Override
    public Integer getFinalRank() {
        if(getEndOfRace() == null) {
            return null;
        }
        return getRankAt(getEndOfRace());
    }

    @Override
    public Pair<Double, Integer> getRelativeDistanceToAdvantageousSideAtStartOfRaceVsFinalRank() {
        return new Pair<>(getRelativeDistanceToAdvantageousEndOfLineAtStartOfRace(), getFinalRank());
    }

    @Override
    public Speed getAverageRaceWindSpeed() {
        return getTrackedRace().getAverageWindSpeedWithConfidence(5000).getObject();
    }

    @Override
    public Double getBiasAtStartOfRace() {
        return getBiasAtTimePoint(getStartOfRace());
    }

    @Override
    public Double getBias30SecondsAfterRaceStart() {
        return getBiasAtTimePoint(getStartOfRace().plus(TimeUnit.SECONDS.toMillis(30)));
    }
    
    private GPSFixTrack<Competitor, GPSFixMoving> getTrackOfCompetitor() {
        return getTrackedRace().getTrack(getCompetitor());
    }

    private TrackedLegOfCompetitor getFirstLegOfCompetitor() {
        return getTrackedRace().getTrackedLeg(competitor, getTrackedRace().getRace().getCourse().getFirstLeg());
    }
    
    private TimePoint getStartOfRace() {
        return getTrackedRace().getStartOfRace();
    }

    private TimePoint getEndOfRace() {
        return getTrackedRace().getEndOfRace();
    }
    
    private Double getNormalizeDistanceToStarboardSideAtTimePoint(TimePoint timepoint) {
        Double distance = getTrackedRace().getDistanceFromStarboardSideOfStartLine(getCompetitor(), timepoint).getMeters();
        Double length = getTrackedRace().getStartLine(timepoint).getLength().getMeters();
        return distance / length;
    }
    
    private Integer getRankAt(TimePoint timePoint) {
        TimePoint startOfRace = getStartOfRace();
        if (startOfRace == null) {
            return null;
        }
        
        Integer rank = getTrackedRace().getRank(getCompetitor(), timePoint);
        return rank == 0 ? null : rank;
    }
    
    public Double getBiasAtTimePoint(TimePoint timePoint) {
        LineDetails startLine = getTrackedRace().getStartLine(timePoint);
        switch (startLine.getAdvantageousSideWhileApproachingLine()) {
        case PORT:
            return startLine.getAdvantage().getMeters() * -1;
        case STARBOARD:
            return startLine.getAdvantage().getMeters();
        }

        return null;
    }

}