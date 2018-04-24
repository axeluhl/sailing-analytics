package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForManeuver;
import com.sap.sailing.windestimation.impl.maneuvergraph.CoarseGrainedPointOfSail;
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
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */true,
                ManeuverAndPolarsBasedWindEstimationTrackImpl.class.getSimpleName());
        this.polarService = polarService;
        this.trackedRace = trackedRace;
    }

    public void analyzeRace() {
//        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
//        ManeuverDetector maneuverDetector = new ManeuverDetectorImpl();
//        for (Competitor competitor : competitors) {
//            Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
//            //TODO make iterative
//            List<CompleteManeuverCurve> completeManeuverCurves = maneuverDetector.getCompleteManeuverCurves(maneuvers);
//            List<CompleteManeuverCurveWithEstimationData> completeManeuverCurvesWithEstimationData = maneuverDetector.getCompleteManeuverCurvesWithEstimationData(completeManeuverCurves);
//            
//            trackedRace.getTrack(competitor).getDistanceTraveled(from, to)
//            
//        }
//        
//        List<WindTrackCandidate> bestWindTrackCandidates = new ArrayList<>();
//        for (Iterable<WindTrackCandidate> currentCandidates : windTrackCandidatesPerCompetitor
//                .values()) {
//            for (Iterable<WindTrackCandidate> otherCandidates : windTrackCandidatesPerCompetitor
//                    .values()) {
//                if(currentCandidates != otherCandidates) {
//                    for(WindTrackCandidate currentCandidate : currentCandidates) {
//                        for(WindTrackCandidate otherCandidate : otherCandidates) {
//                            
//                        }
//                    }
//                }
//            }
//        }

//        for (Iterator<WindTrackCandidate> currentCandidates : windTrackCandidatesPerCompetitor
//                .values()) {
//            for (Iterator<WindTrackCandidate> currentTrackCandidatesForCurrentTimePoint : currentCandidates
//                    .getWindDirectionCandidatesForTimePoints()) {
//                List<WindDirectionCandidatesForManeuver> candidatesForCurrentTimePointFromAllTracks = new ArrayList<>(
//                        windTrackCandidatesPerCompetitor.size());
//                candidatesForCurrentTimePointFromAllTracks.add(currentTrackCandidatesForCurrentTimePoint);
//                for (WindDirectionCandidatesForTimePointIterationHelper candidatesOfOtherTrack : windTrackCandidatesPerCompetitor
//                        .values()) {
//                    if (candidatesOfOtherTrack != currentCandidates) {
//                        WindDirectionCandidatesForManeuver otherCandidatesForCurrentTimePoint = candidatesOfOtherTrack
//                                .getWindDirectionCandidatesWithTimePointClosestTo(
//                                        currentTrackCandidatesForCurrentTimePoint.getTimePoint());
//                        if (otherCandidatesForCurrentTimePoint != null) {
//                            candidatesForCurrentTimePointFromAllTracks.add(otherCandidatesForCurrentTimePoint);
//                        }
//                    }
//                }
//                // determine best wind course for current time point
//                BearingWithConfidence<TimePoint> windCourseWithConfidence = determineBestWindCourseFromWindCourseCandidatesForTimePoint(
//                        candidatesForCurrentTimePointFromAllTracks,
//                        currentTrackCandidatesForCurrentTimePoint.getTimePoint());
//                if (windCourseWithConfidence != null) {
//                    Iterable<BoatClassWithSpeedAndPointOfSail> speedsAndCoursesOfBoats = determineReferenceBoatSpeedForTimePoint(
//                            candidatesForCurrentTimePointFromAllTracks,
//                            currentTrackCandidatesForCurrentTimePoint.getTimePoint());
//                    SpeedWithConfidence<TimePoint> windSpeed = estimateWindSpeedByBoatSpeedAndTwa(
//                            speedsAndCoursesOfBoats);
//                    KnotSpeedWithBearingImpl windSpeedWithBearing = new KnotSpeedWithBearingImpl(
//                            windSpeed.getObject().getKnots(), windCourseWithConfidence.getObject());
//                    Wind wind = new WindImpl(currentTrackCandidatesForCurrentTimePoint.getManeuver().getPosition(),
//                            currentTrackCandidatesForCurrentTimePoint.getTimePoint(), windSpeedWithBearing);
//                    // TODO merge confidences
//                    // WindWithConfidence<TimePoint> wind = new WindWithConfidenceImpl<TimePoint>(wind, confidence,
//                    // relativeTo, useSpeed)
//                    // TODO rethink how to make different confidences for each fix
//                    add(wind);
//                }
//            }
//        }
    }

    private SpeedWithConfidence<TimePoint> estimateWindSpeedByBoatSpeedAndTwa(
            Iterable<BoatClassWithSpeedAndPointOfSail> speedsAndCoursesOfBoats) {
        // TODO polarService.getAverageTrueWindSpeedAndAngleCandidates(boatClass, speedOverGround, legType, tack)
        return null;
    }

    private Iterable<BoatClassWithSpeedAndPointOfSail> determineReferenceBoatSpeedForTimePoint(
            List<WindDirectionCandidatesForManeuver> candidatesForCurrentTimePointFromAllTracks, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    private BearingWithConfidence<TimePoint> determineBestWindCourseFromWindCourseCandidatesForTimePoint(
            List<WindDirectionCandidatesForManeuver> candidatesForTimePointFromAllTracks, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    private static class BoatClassWithSpeedAndPointOfSail {

        private final BoatClass boatClass;
        private final Speed speed;
        private final CoarseGrainedPointOfSail pointOfSail;

        public BoatClassWithSpeedAndPointOfSail(BoatClass boatClass, Speed speed,
                CoarseGrainedPointOfSail pointOfSail) {
            this.boatClass = boatClass;
            this.speed = speed;
            this.pointOfSail = pointOfSail;
        }

        public BoatClass getBoatClass() {
            return boatClass;
        }

        public Speed getSpeed() {
            return speed;
        }

        public CoarseGrainedPointOfSail getPointOfSail() {
            return pointOfSail;
        }

    }

}
