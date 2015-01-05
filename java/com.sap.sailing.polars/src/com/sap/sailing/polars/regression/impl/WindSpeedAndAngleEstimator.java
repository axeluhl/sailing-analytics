package com.sap.sailing.polars.regression.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sse.common.Util.Pair;

public class WindSpeedAndAngleEstimator {

    private static final double DEFAULT_MAX_DISTANCE_TO_SAMPLING_POINT = 0.5;
    private final List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed;
    private final double maxDistanceToSamplingPointFunctionInKnots;

    /**
     * 
     * @param averageBoatSpeedAndCourseForWindSpeed
     *            List of sampling points. Needs to be sorted by windspeed (A of the Pair object) low to high
     */
    public WindSpeedAndAngleEstimator(
            List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed) {
        this(averageBoatSpeedAndCourseForWindSpeed, DEFAULT_MAX_DISTANCE_TO_SAMPLING_POINT);
    }

    /**
     * 
     * @param averageBoatSpeedAndCourseForWindSpeed
     *            List of sampling points. Needs to be sorted by windspeed (A of the Pair object) low to high
     * @param maxDistanceToSamplingPointFunctionInKnots
     *            If there is only a sampling point on one side, what's the maximal distance to consider valid when
     *            comparing?
     */
    public WindSpeedAndAngleEstimator(
            List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed,
            double maxDistanceToSamplingPointFunctionInKnots) {
        this.averageBoatSpeedAndCourseForWindSpeed = averageBoatSpeedAndCourseForWindSpeed;
        this.maxDistanceToSamplingPointFunctionInKnots = maxDistanceToSamplingPointFunctionInKnots;
    }

    /**
     * Looks at all entries in {@link #dataCountAndAngleSumMap} and determines the wind speed and true wind angle at
     * which the boat has most likely been sailing to achieve the <code>boatSpeed</code> provided.
     * 
     * @return null if none was found, otherwise candidate with highest confidence, found in the list returned by
     *         {@link #getAverageTrueWindSpeedAndAngleCandidates(Speed)}
     */
    public SpeedWithBearingWithConfidence<Void> getAverageTrueWindSpeedAndAngle(Speed boatSpeed) {
        Set<SpeedWithBearingWithConfidence<Void>> resultCandidates = getAverageTrueWindSpeedAndAngleCandidates(boatSpeed);
        
        SpeedWithBearingWithConfidence<Void> resultCandidateWithHighestConfidence = null;
        for (SpeedWithBearingWithConfidence<Void> resultCandidate : resultCandidates) {
            if (resultCandidateWithHighestConfidence == null
                    || resultCandidate.getConfidence() > resultCandidateWithHighestConfidence.getConfidence()) {
                resultCandidateWithHighestConfidence = resultCandidate;
            }
        }
        return resultCandidateWithHighestConfidence;
    }


    /**
     * Uses the constructor-supplied sampling points to find wind and course candidates for the supplied boatSpeed.
     * 
     * @return empty set if the underlying data was not sufficient to estimate wind. Otherwise a set of candidates
     *         with windspeed course of the boat relative to the wind and a confidence which was derived from the
     *         confidences of underlying fixes, the amount of underlying fixes and the distance between the input
     *         boatSpeed and the sampling points supplied by the gathered polar data.
     */
    public Set<SpeedWithBearingWithConfidence<Void>> getAverageTrueWindSpeedAndAngleCandidates(Speed boatSpeed) {
        double requestedBoatSpeedInKnots = boatSpeed.getKnots();
        Pair<Speed, SpeedWithBearingWithConfidence<Void>> last = null;
        Set<SpeedWithBearingWithConfidence<Void>> resultCandidates = new HashSet<>();
        Set<SpeedWithBearingWithConfidence<Void>> resultCandidatesWithDistance = new HashSet<>();
        for (Pair<Speed, SpeedWithBearingWithConfidence<Void>> averageSpeedForWindSpeed : averageBoatSpeedAndCourseForWindSpeed) {
            Speed windSpeed = averageSpeedForWindSpeed.getA();
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse = averageSpeedForWindSpeed.getB();
            double currentBoatSpeedInKnots = averageBoatSpeedWithCourse.getObject().getKnots();
            if (last != null) {
                double lastBoatSpeedInKnots = last.getB().getObject().getKnots();
                
                if (lastBoatSpeedInKnots <= requestedBoatSpeedInKnots
                        && currentBoatSpeedInKnots >= requestedBoatSpeedInKnots) {
                    SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = createWeightedResultCandidateForLowerLast(
                            requestedBoatSpeedInKnots, last, windSpeed, averageBoatSpeedWithCourse,
                            lastBoatSpeedInKnots, currentBoatSpeedInKnots);
                    resultCandidates.add(resultCandidate);
                } else if (lastBoatSpeedInKnots >= requestedBoatSpeedInKnots
                        && currentBoatSpeedInKnots <= requestedBoatSpeedInKnots) {
                    SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = createWeightedResultCandidateForHigherLast(
                            requestedBoatSpeedInKnots, last, windSpeed, averageBoatSpeedWithCourse,
                            lastBoatSpeedInKnots, currentBoatSpeedInKnots);
                    resultCandidates.add(resultCandidate);
                }
            }
            /*
             * This will be run when there was no candidate found in between the two sampling points (or we are at the
             * beginning) and add a Candidate if the requested point was maxDistanceToSamplingPointFunctionInKnots or
             * less away from the last regarded sampling point. The candidate (if valid) is then added to the
             * resultCandidatesWithDistance set. Confidences are halved and the set is only used if the standard result
             * set is empty in the end
             */
            if (resultCandidates.size() == 0 && last != null) {
                addResultCandidateIfDistanceValid(requestedBoatSpeedInKnots, resultCandidatesWithDistance, last.getA(),
                        last.getB());
            }
            last = averageSpeedForWindSpeed;
        }
        // check last sampling point for distance match and return withDistance set if standard set is empty
        if (resultCandidates.size() == 0) {
            addResultCandidateIfDistanceValid(requestedBoatSpeedInKnots, resultCandidatesWithDistance, last.getA(),
                    last.getB());
            resultCandidates = resultCandidatesWithDistance;
        }
        return resultCandidates;
    }

    /**
     * Confidence will be halved in situations where there the requested speed does not lie between any sampling points.
     * The speed and angle values are equal to the sampling point values.
     */
    private void addResultCandidateIfDistanceValid(double requestedBoatSpeedInKnots,
            Set<SpeedWithBearingWithConfidence<Void>> resultCandidates, Speed windSpeed,
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse) {
        if (Math.abs(averageBoatSpeedWithCourse.getObject().getKnots() - requestedBoatSpeedInKnots) <= maxDistanceToSamplingPointFunctionInKnots) {
            SpeedWithBearingWithConfidence<Void> resultCandidate = new SpeedWithBearingWithConfidenceImpl<Void>(
                    new KnotSpeedWithBearingImpl(windSpeed.getKnots(), new DegreeBearingImpl(averageBoatSpeedWithCourse
                            .getObject().getBearing().getDegrees())), averageBoatSpeedWithCourse.getConfidence() * 0.5,
                    null);
            resultCandidates.add(resultCandidate);
        }
    }

    private SpeedWithBearingWithConfidenceImpl<Void> createWeightedResultCandidateForHigherLast(
            double requestedBoatSpeedInKnots, Pair<Speed, SpeedWithBearingWithConfidence<Void>> last, Speed windSpeed,
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse, double lastBoatSpeedInKnots,
            double currentBoatSpeedInKnots) {
        double differenceBetweenBothSpeedsInKnots = lastBoatSpeedInKnots - currentBoatSpeedInKnots;
        double ratio;
        if (differenceBetweenBothSpeedsInKnots < 0.00001) {
            //Both sampling points have the same ratio. Use confidence to determine the ratio
            ratio = last.getB().getConfidence() / (averageBoatSpeedWithCourse.getConfidence() + last.getB().getConfidence());
        } else {
            double differenceBetweenRequestedAndLower = requestedBoatSpeedInKnots - currentBoatSpeedInKnots;
            ratio = differenceBetweenRequestedAndLower / differenceBetweenBothSpeedsInKnots;
        }
        double weightedAverageWindSpeedInKnots = (( 1 - ratio) * windSpeed.getKnots())
                + (ratio * last.getA().getKnots());
        double currentCourseInDeg = averageBoatSpeedWithCourse.getObject().getBearing()
                .getDegrees();
        double lastCourseInDeg = last.getB().getObject().getBearing().getDegrees();
        double weightedAverageAngleInDeg = (( 1 - ratio) * currentCourseInDeg) + (ratio * lastCourseInDeg);
        double weightedAverageConfidence = (( 1 - ratio) * averageBoatSpeedWithCourse.getConfidence())
                + (ratio * last.getB().getConfidence());
        SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = new SpeedWithBearingWithConfidenceImpl<Void>(
                new KnotSpeedWithBearingImpl(weightedAverageWindSpeedInKnots, new DegreeBearingImpl(
                        weightedAverageAngleInDeg)), weightedAverageConfidence, null);
        return resultCandidate;
    }

    private SpeedWithBearingWithConfidenceImpl<Void> createWeightedResultCandidateForLowerLast(
            double requestedBoatSpeedInKnots, Pair<Speed, SpeedWithBearingWithConfidence<Void>> last, Speed windSpeed,
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse, double lastBoatSpeedInKnots,
            double currentBoatSpeedInKnots) {
        double differenceBetweenBothSpeedsInKnots = currentBoatSpeedInKnots - lastBoatSpeedInKnots;
        double ratio;
        if (differenceBetweenBothSpeedsInKnots < 0.00001) {
            // Both sampling points have the same ratio. Use confidence to determine the ratio
            ratio = averageBoatSpeedWithCourse.getConfidence()
                    / (averageBoatSpeedWithCourse.getConfidence() + last.getB().getConfidence());
        } else {
            double differenceBetweenRequestedAndLower = requestedBoatSpeedInKnots - lastBoatSpeedInKnots;
            ratio = differenceBetweenRequestedAndLower / differenceBetweenBothSpeedsInKnots;
        }
        double weightedAverageWindSpeedInKnots = (ratio * windSpeed.getKnots())
                + ((1 - ratio) * last.getA().getKnots());
        double currentCourseInDeg = averageBoatSpeedWithCourse.getObject().getBearing()
                .getDegrees();
        double lastCourseInDeg = last.getB().getObject().getBearing().getDegrees();
        double weightedAverageAngleInDeg = (ratio * currentCourseInDeg) + ((1 - ratio) * lastCourseInDeg);
        double weightedAverageConfidence = (ratio * averageBoatSpeedWithCourse.getConfidence())
                + ((1 - ratio) * last.getB().getConfidence());
        SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = new SpeedWithBearingWithConfidenceImpl<Void>(
                new KnotSpeedWithBearingImpl(weightedAverageWindSpeedInKnots, new DegreeBearingImpl(
                        weightedAverageAngleInDeg)), weightedAverageConfidence, null);
        return resultCandidate;
    }

}
