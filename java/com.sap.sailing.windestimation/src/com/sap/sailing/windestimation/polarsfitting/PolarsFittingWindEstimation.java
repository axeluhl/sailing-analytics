package com.sap.sailing.windestimation.polarsfitting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.AverageWindEstimator;
import com.sap.sailing.windestimation.data.CoarseGrainedPointOfSail;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class PolarsFittingWindEstimation implements AverageWindEstimator {

    private static final int COURSE_CLUSTER_SIZE = 5;

    private final PolarDataService polarService;
    private final Map<BoatClass, SpeedStatistics[]> speedsPerBoatClass = new HashMap<>();

    public PolarsFittingWindEstimation(PolarDataService polarService) {
        this.polarService = polarService;
    }

    public PolarsFittingWindEstimation(PolarDataService polarService,
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks) {
        this(polarService);
        for (CompetitorTrackWithEstimationData<ManeuverForEstimation> competitorTrack : competitorTracks) {
            for (ManeuverForEstimation maneuver : competitorTrack.getElements()) {
                if (maneuver.isCleanBefore()) {
                    addSpeedWithCourseRecord(maneuver.getAverageSpeedWithBearingBefore(),
                            competitorTrack.getBoatClass());
                }
                if (maneuver.isCleanAfter()) {
                    addSpeedWithCourseRecord(maneuver.getAverageSpeedWithBearingAfter(),
                            competitorTrack.getBoatClass());
                }
            }
        }
    }

    public void addSpeedWithCourseRecord(SpeedWithBearing speedWithCourse, BoatClass boatClass) {
        SpeedStatistics speedStatistics = getSpeedStatistics(boatClass, speedWithCourse.getBearing().getDegrees());
        speedStatistics.addSpeed(speedWithCourse.getKnots());
    }

    private SpeedStatistics getSpeedStatistics(BoatClass boatClass, double courseInDegrees) {
        int index = ((int) courseInDegrees) / COURSE_CLUSTER_SIZE;
        SpeedStatistics[] speedStatisticsPerCourseCluster = speedsPerBoatClass.get(boatClass);
        if (speedStatisticsPerCourseCluster == null) {
            speedStatisticsPerCourseCluster = new SpeedStatistics[360 / COURSE_CLUSTER_SIZE];
            speedsPerBoatClass.put(boatClass, speedStatisticsPerCourseCluster);
        }
        SpeedStatistics speedStatistics = speedStatisticsPerCourseCluster[index];
        if (speedStatistics == null) {
            speedStatistics = new SpeedStatistics();
            speedStatisticsPerCourseCluster[index] = speedStatistics;
        }
        return speedStatistics;
    }

    public SpeedWithBearingWithConfidence<Void> estimateWind() {
        List<SpeedWithBearingWithConfidence<Double>> windCandidates = new ArrayList<>();
        for (int trueWindCourseCandidateInDegrees = 0; trueWindCourseCandidateInDegrees < 360; trueWindCourseCandidateInDegrees += 5) {
            int totalSpeeds = 0;
            int speedsWithinDeadWindZone = 0;
            List<Pair<WindSpeedRange, Integer>> windSpeedRangesWithFixesCount = new ArrayList<>();
            int[] pointOfSailCounts = new int[CoarseGrainedPointOfSail.values().length];
            for (Entry<BoatClass, SpeedStatistics[]> speedsForBoatClassEntry : speedsPerBoatClass.entrySet()) {
                BoatClass boatClass = speedsForBoatClassEntry.getKey();
                SpeedStatistics[] speedsForBoatClass = speedsForBoatClassEntry.getValue();
                Bearing trueWindCourseCandidate = new DegreeBearingImpl(trueWindCourseCandidateInDegrees);
                for (int i = 0; i < 360 / COURSE_CLUSTER_SIZE; i++) {
                    SpeedStatistics speedStatistics = speedsForBoatClass[i];
                    if (speedStatistics != null && speedStatistics.getSpeedsCount() > 0
                            && speedStatistics.getAvgSpeed() > 2) {
                        Bearing boatCourse = new DegreeBearingImpl(i * COURSE_CLUSTER_SIZE);
                        Bearing twa = trueWindCourseCandidate.reverse().getDifferenceTo(boatCourse);
                        double absTwaInDegrees = Math.abs(twa.getDegrees());
                        double avgSpeedInKnots = speedStatistics.getAvgSpeed();
                        WindSpeedRange windSpeedRange = getWindSpeedRange(boatClass, avgSpeedInKnots, absTwaInDegrees);
                        if (windSpeedRange != null) {
                            FineGrainedPointOfSail pointOfSail = FineGrainedPointOfSail.valueOf(twa.getDegrees());
                            pointOfSailCounts[pointOfSail.getCoarseGrainedPointOfSail().ordinal()] = speedStatistics
                                    .getSpeedsCount();
                            windSpeedRangesWithFixesCount
                                    .add(new Pair<>(windSpeedRange, speedStatistics.getSpeedsCount()));
                            totalSpeeds += speedStatistics.getSpeedsCount();
                            if (absTwaInDegrees <= 20) {
                                speedsWithinDeadWindZone += speedStatistics.getSpeedsCount();
                            }
                        }
                    }
                }
            }
            SpeedWithConfidence<Void> windSpeedCandidate = calculateWindCandidate(windSpeedRangesWithFixesCount,
                    totalSpeeds, speedsWithinDeadWindZone);
            if (windSpeedCandidate != null) {
                double confidenceFactor = calculatePointOfSailCountsConfidenceFactor(pointOfSailCounts);
                windCandidates.add(new SpeedWithBearingWithConfidenceImpl<>(
                        new KnotSpeedWithBearingImpl(windSpeedCandidate.getObject().getKnots(),
                                new DegreeBearingImpl(trueWindCourseCandidateInDegrees)),
                        windSpeedCandidate.getConfidence(), confidenceFactor));
            }
        }
        SpeedWithBearingWithConfidence<Void> bestCandidate = determineBestWindCandidateAndCalculateConfidence(
                windCandidates);
        return bestCandidate;
    }

    private double calculatePointOfSailCountsConfidenceFactor(int[] pointOfSailCounts) {
        double[] bonusFactors = new double[pointOfSailCounts.length];
        int containsUpwind = 0;
        int containsReaching = 0;
        int containsDownwind = 0;
        for (CoarseGrainedPointOfSail pointOfSail : CoarseGrainedPointOfSail.values()) {
            int count = pointOfSailCounts[pointOfSail.ordinal()];
            double bonusFactor = 1.0 / 10 - Math.min(9, count);
            if (bonusFactor > 0.001) {
                bonusFactors[pointOfSail.ordinal()] = bonusFactor;
                switch (pointOfSail.getLegType()) {
                case UPWIND:
                    containsUpwind++;
                    break;
                case REACHING:
                    containsReaching++;
                    break;
                case DOWNWIND:
                    containsDownwind++;
                    break;
                }
            }
        }
        double baseConfidence = 0;
        if (containsUpwind > 0 && (containsReaching > 0 || containsDownwind > 0)) {
            baseConfidence += 0.5;
            if (containsReaching > 0 && containsDownwind > 0 || containsUpwind > 1 || containsDownwind > 1) {
                baseConfidence += 0.25;
                if (containsDownwind > 1 && containsUpwind > 1) {
                    baseConfidence += 0.25;
                }
            }
        } else if (containsDownwind > 0 && containsReaching > 0) {
            baseConfidence += 0.25;
        }

        double confidence = 0;
        for (double bonusFactor : bonusFactors) {
            confidence += bonusFactor / bonusFactors.length * baseConfidence;
        }
        return confidence;
    }

    private SpeedWithBearingWithConfidence<Void> determineBestWindCandidateAndCalculateConfidence(
            List<SpeedWithBearingWithConfidence<Double>> windCandidates) {
        SpeedWithBearingWithConfidence<Double> bestWindCandidate = null;
        for (SpeedWithBearingWithConfidence<Double> windCandidate : windCandidates) {
            if (bestWindCandidate == null || bestWindCandidate.getConfidence() < windCandidate.getConfidence()) {
                bestWindCandidate = windCandidate;
            }
        }

        double bestChallengingConfidence = 0;

        for (SpeedWithBearingWithConfidence<Double> windCandidate : windCandidates) {
            double bearingToBestCandidate = windCandidate.getObject().getBearing()
                    .getDifferenceTo(bestWindCandidate.getObject().getBearing()).getDegrees();
            double absBearingToBestCandidate = Math.abs(bearingToBestCandidate);
            if (absBearingToBestCandidate >= 45 && bestChallengingConfidence < windCandidate.getConfidence()) {
                bestChallengingConfidence = windCandidate.getConfidence();
            }
        }
        if (bestWindCandidate == null) {
            return null;
        }
        double realConfidence = (1
                - (bestWindCandidate.getConfidence() - bestChallengingConfidence) / bestWindCandidate.getConfidence())
                * bestWindCandidate.getConfidence();

        return new SpeedWithBearingWithConfidenceImpl<Void>(bestWindCandidate.getObject(), realConfidence, null);
    }

    private SpeedWithConfidence<Void> calculateWindCandidate(
            List<Pair<WindSpeedRange, Integer>> windSpeedRangesWithFixesCount, int totalSpeeds,
            int speedsWithinDeadWindZone) {
        WindSpeedRange extendedWindSpeedRange = null;
        for (Pair<WindSpeedRange, Integer> windSpeedRangeWithFixesCount : windSpeedRangesWithFixesCount) {
            extendedWindSpeedRange = extendedWindSpeedRange == null ? windSpeedRangeWithFixesCount.getA()
                    : extendedWindSpeedRange.extend(windSpeedRangeWithFixesCount.getA());
        }
        if (extendedWindSpeedRange == null) {
            return null;
        }
        double upperSpeed = Math.min(50.0, extendedWindSpeedRange.getUpperSpeed());
        double lowerSpeed = Math.max(1.0, extendedWindSpeedRange.getLowerSpeed());
        if (upperSpeed < lowerSpeed) {
            double temp = upperSpeed;
            upperSpeed = lowerSpeed;
            lowerSpeed = temp;
        }
        double bestSpeed = 0;
        double bestSquaredDeviationSum = Double.MAX_VALUE;
        for (double speed = lowerSpeed; speed <= upperSpeed; speed += 0.1) {
            double squaredDeviationSum = 0;
            for (Pair<WindSpeedRange, Integer> windSpeedRangeWithFixesCount : windSpeedRangesWithFixesCount) {
                double deviationOfSpeedFromRange = windSpeedRangeWithFixesCount.getA()
                        .getDeviationOfSpeedFromRange(speed);
                double relevanceFactor = 1.0 * windSpeedRangeWithFixesCount.getB() / totalSpeeds;
                squaredDeviationSum += deviationOfSpeedFromRange * deviationOfSpeedFromRange * relevanceFactor;
            }
            squaredDeviationSum *= 1 - 1.0 * speedsWithinDeadWindZone / totalSpeeds;
            if (bestSquaredDeviationSum > squaredDeviationSum) {
                bestSpeed = speed;
                bestSquaredDeviationSum = squaredDeviationSum;
            }
        }
        return new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(bestSpeed), 1 / bestSquaredDeviationSum, null);
    }

    public WindSpeedRange getWindSpeedRange(BoatClass boatClass, double avgSpeedInKnots, double absTwaInDegrees) {
        Pair<List<Speed>, Double> windSpeeds;
        try {
            windSpeeds = polarService.estimateWindSpeeds(boatClass, new KnotSpeedImpl(avgSpeedInKnots),
                    new DegreeBearingImpl(absTwaInDegrees));
        } catch (NotEnoughDataHasBeenAddedException e) {
            return null;
        }
        double minSpeed = 0;
        double maxSpeed = 0;
        for (Speed speed : windSpeeds.getA()) {
            double speedInKnots = speed.getKnots();
            if (speedInKnots > 2) {
                if (minSpeed == 0 || minSpeed > speedInKnots) {
                    minSpeed = speedInKnots;
                }
                if (maxSpeed == 0 || maxSpeed < speedInKnots) {
                    maxSpeed = speedInKnots;
                }
            }
        }
        // if(maxSpeed - minSpeed > 2) {
        // maxSpeed = minSpeed + 2;
        // }
        return minSpeed == 0 ? null : new WindSpeedRange(minSpeed, maxSpeed);
    }

    @Override
    public WindWithConfidence<Void> estimateAverageWind() {
        SpeedWithBearingWithConfidence<Void> wind = estimateWind();
        WindWithConfidenceImpl<Void> windWithConfidence = null;
        if (wind != null) {
            windWithConfidence = new WindWithConfidenceImpl<Void>(new WindImpl(null, null, wind.getObject()),
                    wind.getConfidence(), null, wind.getObject().getKnots() > 0);
        }
        return windWithConfidence;
    }

    public Speed getWindSpeed(ManeuverForEstimation maneuver, Bearing windCourse) {
        WindSpeedRange windSpeedRange = null;
        BoatClass boatClass = maneuver.getBoatClass();
        if (maneuver.isCleanBefore()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse()
                    .getDifferenceTo(maneuver.getAverageSpeedWithBearingBefore().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingBefore().getKnots();
            windSpeedRange = getWindSpeedRange(boatClass, avgSpeedInKnots, absTwaInDegrees);
        }
        if (maneuver.isCleanAfter()) {
            double absTwaInDegrees = Math.abs(windCourse.reverse()
                    .getDifferenceTo(maneuver.getAverageSpeedWithBearingAfter().getBearing()).getDegrees());
            double avgSpeedInKnots = maneuver.getAverageSpeedWithBearingAfter().getKnots();
            WindSpeedRange currentTwaWindSpeedRange = getWindSpeedRange(boatClass, avgSpeedInKnots, absTwaInDegrees);
            if (currentTwaWindSpeedRange != null) {
                windSpeedRange = windSpeedRange == null ? currentTwaWindSpeedRange
                        : windSpeedRange.extend(currentTwaWindSpeedRange);
            }
        }
        if (windSpeedRange != null) {
            return new KnotSpeedImpl(windSpeedRange.getMiddleSpeed());
        }
        return new KnotSpeedImpl(0.0);
    }

}
