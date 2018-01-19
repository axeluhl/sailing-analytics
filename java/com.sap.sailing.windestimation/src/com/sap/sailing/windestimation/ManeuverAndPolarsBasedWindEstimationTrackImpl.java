package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.impl.CompetitorManeuverBasedWindDirectionEstimator;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForManeuver;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForTimePointIterationHelper;
import com.sap.sailing.windestimation.impl.graph.PointOfSailWithTack;
import com.sap.sse.common.TimePoint;

public class ManeuverAndPolarsBasedWindEstimationTrackImpl extends WindTrackImpl {

    private static final long serialVersionUID = 7134653811016476998L;
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a default and should be
     * superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;

    private final PolarDataService polarService;
    private final TrackedRace trackedRace;

    public ManeuverAndPolarsBasedWindEstimationTrackImpl(PolarDataService polarService, TrackedRace trackedRace,
            long millisecondsOverWhichToAverage, boolean waitForLatest) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */true, ManeuverAndPolarsBasedWindEstimationTrackImpl.class.getSimpleName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
    }
    
    public void analyzeRace() {
        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        CompetitorManeuverBasedWindDirectionEstimator competitorManeuverBasedWindEstimator = new CompetitorManeuverBasedWindDirectionEstimator();
        Map<Competitor, WindDirectionCandidatesForTimePointIterationHelper> windDirectionCandidatesPerCompetitorTrack = new HashMap<>();
        for (Competitor competitor : competitors) {
            Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
            Iterable<WindDirectionCandidatesForManeuver> windDirectionCandidates = competitorManeuverBasedWindEstimator.computeWindDirectionCandidates(maneuvers);
            windDirectionCandidatesPerCompetitorTrack.put(competitor, new WindDirectionCandidatesForTimePointIterationHelper(windDirectionCandidates));
        }
        
        for (WindDirectionCandidatesForTimePointIterationHelper currentCandidates : windDirectionCandidatesPerCompetitorTrack.values()) {
            for (WindDirectionCandidatesForManeuver currentTrackCandidatesForCurrentTimePoint : currentCandidates.getWindDirectionCandidatesForTimePoints()) {
                List<WindDirectionCandidatesForManeuver> candidatesForCurrentTimePointFromAllTracks = new ArrayList<>(windDirectionCandidatesPerCompetitorTrack.size());
                candidatesForCurrentTimePointFromAllTracks.add(currentTrackCandidatesForCurrentTimePoint);
                for (WindDirectionCandidatesForTimePointIterationHelper candidatesOfOtherTrack : windDirectionCandidatesPerCompetitorTrack.values()) {
                    if(candidatesOfOtherTrack != currentCandidates) {
                        WindDirectionCandidatesForManeuver otherCandidatesForCurrentTimePoint = candidatesOfOtherTrack.getWindDirectionCandidatesWithTimePointClosestTo(currentTrackCandidatesForCurrentTimePoint.getTimePoint());
                        if(otherCandidatesForCurrentTimePoint != null) {
                            candidatesForCurrentTimePointFromAllTracks.add(otherCandidatesForCurrentTimePoint);
                        }
                    }
                }
                //determine best wind course for current time point
                BearingWithConfidence<TimePoint> windCourseWithConfidence = determineBestWindCourseFromWindCourseCandidatesForTimePoint(candidatesForCurrentTimePointFromAllTracks, currentTrackCandidatesForCurrentTimePoint.getTimePoint());
                if(windCourseWithConfidence != null) {
                    Iterable<BoatClassWithSpeedAndPointOfSailWithTack> speedsAndCoursesOfBoats = determineReferenceBoatSpeedForTimePoint(candidatesForCurrentTimePointFromAllTracks, currentTrackCandidatesForCurrentTimePoint.getTimePoint());
                    SpeedWithConfidence<TimePoint> windSpeed = estimateWindSpeedByBoatSpeedAndTwa(speedsAndCoursesOfBoats);
                    KnotSpeedWithBearingImpl windSpeedWithBearing = new KnotSpeedWithBearingImpl(windSpeed.getObject().getKnots(), windCourseWithConfidence.getObject());
                    Wind wind = new WindImpl(currentTrackCandidatesForCurrentTimePoint.getManeuver().getPosition(), currentTrackCandidatesForCurrentTimePoint.getTimePoint(), windSpeedWithBearing);
                    //TODO merge confidences
//                    WindWithConfidence<TimePoint> wind = new WindWithConfidenceImpl<TimePoint>(wind, confidence, relativeTo, useSpeed)
                    //TODO rethink how to make different confidences for each fix
                    add(wind);
                }
            }
        }
    }

    private SpeedWithConfidence<TimePoint> estimateWindSpeedByBoatSpeedAndTwa(
            Iterable<BoatClassWithSpeedAndPointOfSailWithTack> speedsAndCoursesOfBoats) {
        // TODO polarService.getAverageTrueWindSpeedAndAngleCandidates(boatClass, speedOverGround, legType, tack)
        return null;
    }

    private Iterable<BoatClassWithSpeedAndPointOfSailWithTack> determineReferenceBoatSpeedForTimePoint(
            List<WindDirectionCandidatesForManeuver> candidatesForCurrentTimePointFromAllTracks, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    private BearingWithConfidence<TimePoint> determineBestWindCourseFromWindCourseCandidatesForTimePoint(
            List<WindDirectionCandidatesForManeuver> candidatesForTimePointFromAllTracks, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static class BoatClassWithSpeedAndPointOfSailWithTack {
        
        private final BoatClass boatClass;
        private final Speed speed;
        private final PointOfSailWithTack pointOfSailWithTack;

        public BoatClassWithSpeedAndPointOfSailWithTack(BoatClass boatClass, Speed speed, PointOfSailWithTack pointOfSailWithTack) {
            this.boatClass = boatClass;
            this.speed = speed;
            this.pointOfSailWithTack = pointOfSailWithTack;
        }

        public BoatClass getBoatClass() {
            return boatClass;
        }

        public Speed getSpeed() {
            return speed;
        }

        public PointOfSailWithTack getPointOfSailWithTack() {
            return pointOfSailWithTack;
        }
        
    }
    
}
