package com.sap.sailing.polars.analysis.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

/**
 * Extracts typical measures from polar sheets.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarSheetAnalyzerImpl implements PolarSheetAnalyzer {

    private static final int MINIMUM_DATA_COUNT_FOR_ONE_ANGLE = 5;
    private final PolarDataService polarDataService;

    public PolarSheetAnalyzerImpl(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedAndCourseOverGround(BoatClass boatClass, Speed windSpeed, LegType legType, Tack tack)
            throws NotEnoughDataHasBeenAddedException {
        final int startAngleInclusive;
        final int endAngleExclusive;
        if (legType == LegType.UPWIND && tack == Tack.STARBOARD) {
            startAngleInclusive = 1;
            endAngleExclusive = 90;
        } else if (legType == LegType.DOWNWIND && tack == Tack.STARBOARD) {
            startAngleInclusive = 91;
            endAngleExclusive = 180;
        } else if (legType == LegType.UPWIND && tack == Tack.PORT) {
            startAngleInclusive = 181;
            endAngleExclusive = 270;
        } else if (legType == LegType.DOWNWIND && tack == Tack.STARBOARD) {
            startAngleInclusive = 271;
            endAngleExclusive = 360;
        } else {
            throw new IllegalArgumentException("Leg type must be "+LegType.UPWIND.name()+
                    " or "+LegType.DOWNWIND.name()+" but was "+legType.name());
        }
        SpeedWithBearingWithConfidence<Void> speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }
    
    private SpeedWithBearingWithConfidence<Void> estimateAnglePeakAndAverageSpeed(BoatClass boatClass, Speed windSpeed,
            int startAngleInclusive, int endAngleExclusive) throws NotEnoughDataHasBeenAddedException {
        int[] dataCountPerAngle = getDataCountArray(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
        double estimatedPeak = estimatePeak(boatClass, windSpeed, startAngleInclusive, endAngleExclusive, dataCountPerAngle);
        double convertedAngleIfOver180 = convertAngleIfNecessary(estimatedPeak);
        SpeedWithConfidence<Void> averagedSpeedWithConfidence = estimateSpeed(boatClass, windSpeed, convertedAngleIfOver180);
        double originConfidence = averagedSpeedWithConfidence.getConfidence();
        double overallConfidence = (originConfidence + calcDataCountConfidence(dataCountPerAngle, estimatedPeak)) / 2;
        Bearing bearing = new DegreeBearingImpl(convertedAngleIfOver180);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(averagedSpeedWithConfidence.getObject().getKnots(), bearing);
        return new SpeedWithBearingWithConfidenceImpl<>(speedWithBearing, overallConfidence, null);
    }

    /**
     * Uses the formula 1-​e^​((‑x)/​150) to create a confidence for the estimated peak. 
     * 
     * If the peak has 100 data points underlying, the confidence will be approx. 0.5
     * 
     * @param dataCountPerAngle
     * @param estimatedPeak
     * @return
     */
    private double calcDataCountConfidence(int[] dataCountPerAngle, double estimatedPeak) {
        int dataCount = dataCountPerAngle[(int) Math.round(estimatedPeak)];
        double confidence = 1 - Math.pow(Math.E, - dataCount / 150);
        return confidence;
    }

    private int[] getDataCountArray(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive) {
        int[] dataCountPerAngle = polarDataService.getDataCountsForWindSpeed(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
        return dataCountPerAngle;
    }

    private double convertAngleIfNecessary(double estimatedPeak) {
        double convertedAngleIfOver180 = estimatedPeak;
        if (estimatedPeak > 180) {
            convertedAngleIfOver180 = estimatedPeak - 360;
        }
        return convertedAngleIfOver180;
    }

    private SpeedWithConfidence<Void> estimateSpeed(BoatClass boatClass, Speed windSpeed, double estimatedPeak) throws NotEnoughDataHasBeenAddedException {
        SpeedWithConfidence<Void> speed = polarDataService.getSpeed(boatClass, windSpeed, new DegreeBearingImpl(
                estimatedPeak));
        return speed;
    }

    private double estimatePeak(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive, int[] dataCountPerAngle)
            throws NotEnoughDataHasBeenAddedException {
        // Find peak by averaging the angles that have at least 50% of the max datacount
        int maxDataCount = Integer.MIN_VALUE;
        for (int i = startAngleInclusive; i < endAngleExclusive; i++) {
            if (dataCountPerAngle[i] > maxDataCount) {
                maxDataCount = dataCountPerAngle[i];
            }
        }
        if (maxDataCount < MINIMUM_DATA_COUNT_FOR_ONE_ANGLE) {
            // The angle with the most data points doesn't have sufficient data, for the polar data to have any
            // significance.
            throw new NotEnoughDataHasBeenAddedException("Only " + maxDataCount
                    + " points have been added for the angle with the most points. No significance.");
        }
        int sumOfAllUpperHalfAngles = 0;
        int numberOfAnglesAdded = 0;
        for (int i = startAngleInclusive; i < endAngleExclusive; i++) {
            int dataCount = dataCountPerAngle[i];
            if (dataCount > maxDataCount / 2) {
                sumOfAllUpperHalfAngles = sumOfAllUpperHalfAngles + i * dataCount;
                numberOfAnglesAdded = numberOfAnglesAdded + dataCount;
            }
        }
        double estimatedPeak = (double) sumOfAllUpperHalfAngles / (double) numberOfAnglesAdded;
        return estimatedPeak;
    }
}
