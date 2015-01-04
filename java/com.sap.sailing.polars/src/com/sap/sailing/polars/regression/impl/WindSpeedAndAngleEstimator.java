package com.sap.sailing.polars.regression.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sse.common.Util.Pair;

public class WindSpeedAndAngleEstimator {

    private static final double DEFAULT_MAX_DISTANCE_TO_SAMPLING_POINT = 0.5;
    private final List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed;
    private final double maxDistanceToSamplingPointIfOnlyOnOneSideInKnots;

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
     * @param maxDistanceToSamplingPointIfOnlyOnOneSideInKnots
     *            If there is only a sampling point on one side, what's the maximal distance to consider valid when
     *            comparing?
     */
    public WindSpeedAndAngleEstimator(
            List<Pair<Speed, SpeedWithBearingWithConfidence<Void>>> averageBoatSpeedAndCourseForWindSpeed,
            double maxDistanceToSamplingPointIfOnlyOnOneSideInKnots) {
        this.averageBoatSpeedAndCourseForWindSpeed = averageBoatSpeedAndCourseForWindSpeed;
        this.maxDistanceToSamplingPointIfOnlyOnOneSideInKnots = maxDistanceToSamplingPointIfOnlyOnOneSideInKnots;
    }

    /**
     * Looks at all entries in {@link #dataCountAndAngleSumMap} and determines the wind speed and true wind angle at
     * which the boat has most likely been sailing to achieve the <code>boatSpeed</code> provided.
     */
    public SpeedWithBearingWithConfidence<Void> getAverageTrueWindSpeedAndAngle(Speed boatSpeed) {
        double requestedBoatSpeedInKnots = boatSpeed.getKnots();
        Pair<Speed, SpeedWithBearingWithConfidence<Void>> last = null;
        List<SpeedWithBearingWithConfidence<Void>> resultCandidates = new ArrayList<>();
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
            } else {
                //This will be run when there is no lower sampling point windspeed wise
                addResultCandidateForOneSideSamplingPointSituationIfDistanceValid(requestedBoatSpeedInKnots,
                        resultCandidates, windSpeed, averageBoatSpeedWithCourse, currentBoatSpeedInKnots);
            }
            last = averageSpeedForWindSpeed;
        }
        // This will be run when there is no higher sampling point windspeed wise
        addResultCandidateForOneSideSamplingPointSituationIfDistanceValid(requestedBoatSpeedInKnots, resultCandidates,
                last.getA(), last.getB(), last.getB().getObject().getKnots());
        
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
     * Confidence will be halved in situations where there is only one sampling point either to the left or the right of
     * the regarded windspeed
     */
    private void addResultCandidateForOneSideSamplingPointSituationIfDistanceValid(double requestedBoatSpeedInKnots,
            List<SpeedWithBearingWithConfidence<Void>> resultCandidates, Speed windSpeed,
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse, double currentBoatSpeedInKnots) {
        if (Math.abs(currentBoatSpeedInKnots - requestedBoatSpeedInKnots) <= maxDistanceToSamplingPointIfOnlyOnOneSideInKnots) {
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
        double differenceBetweenRequestedAndLower = requestedBoatSpeedInKnots - currentBoatSpeedInKnots;
        double ratio = differenceBetweenRequestedAndLower / differenceBetweenBothSpeedsInKnots;
        double weightedAverageWindSpeedInKnots = (( 1 - ratio) * windSpeed.getKnots())
                + (ratio * last.getA().getKnots());
        double currentCourseInDeg = averageBoatSpeedWithCourse.getObject().getBearing()
                .getDegrees();
        double lastCourseInDeg = last.getB().getObject().getBearing().getDegrees();
        double weightedAverageAngleInDeg = (( 1 - ratio) * currentCourseInDeg) + (ratio * lastCourseInDeg);
        double weightedAverageConfidence = (( 1 - ratio) * averageBoatSpeedWithCourse.getConfidence())
                + (ratio * last.getB().getConfidence());
        SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(
                weightedAverageWindSpeedInKnots, new DegreeBearingImpl(weightedAverageAngleInDeg)),
                weightedAverageConfidence, null);
        return resultCandidate;
    }

    private SpeedWithBearingWithConfidenceImpl<Void> createWeightedResultCandidateForLowerLast(
            double requestedBoatSpeedInKnots, Pair<Speed, SpeedWithBearingWithConfidence<Void>> last, Speed windSpeed,
            SpeedWithBearingWithConfidence<Void> averageBoatSpeedWithCourse, double lastBoatSpeedInKnots,
            double currentBoatSpeedInKnots) {
        double differenceBetweenBothSpeedsInKnots = currentBoatSpeedInKnots - lastBoatSpeedInKnots;
        double differenceBetweenRequestedAndLower = requestedBoatSpeedInKnots - lastBoatSpeedInKnots;
        double ratio = differenceBetweenRequestedAndLower / differenceBetweenBothSpeedsInKnots;
        double weightedAverageWindSpeedInKnots = (ratio * windSpeed.getKnots())
                + ((1 - ratio) * last.getA().getKnots());
        double currentCourseInDeg = averageBoatSpeedWithCourse.getObject().getBearing()
                .getDegrees();
        double lastCourseInDeg = last.getB().getObject().getBearing().getDegrees();
        double weightedAverageAngleInDeg = (ratio * currentCourseInDeg) + ((1 - ratio) * lastCourseInDeg);
        double weightedAverageConfidence = (ratio * averageBoatSpeedWithCourse.getConfidence())
                + ((1 - ratio) * last.getB().getConfidence());
        SpeedWithBearingWithConfidenceImpl<Void> resultCandidate = new SpeedWithBearingWithConfidenceImpl<Void>(new KnotSpeedWithBearingImpl(
                weightedAverageWindSpeedInKnots, new DegreeBearingImpl(weightedAverageAngleInDeg)),
                weightedAverageConfidence, null);
        return resultCandidate;
    }

}
