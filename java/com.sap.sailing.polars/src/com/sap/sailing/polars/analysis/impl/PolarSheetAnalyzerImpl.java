package com.sap.sailing.polars.analysis.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
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
    private PolarDataService polarDataService;

    public PolarSheetAnalyzerImpl(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed, boolean useLinReg)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 1;
        int endAngleExclusive = 90;
        SpeedWithBearingWithConfidence<Void> speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive, useLinReg);
        return speedWithBearing;
    }

    
    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed, boolean useLinReg)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 91;
        int endAngleExclusive = 180;
        SpeedWithBearingWithConfidence<Void> speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive, useLinReg);
        return speedWithBearing;
    }
    
    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed, boolean useLinReg)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 181;
        int endAngleExclusive = 270;
        SpeedWithBearingWithConfidence<Void> speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive, useLinReg);
        return speedWithBearing;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed, boolean useLinReg)
        throws NotEnoughDataHasBeenAddedException {
            int startAngleInclusive = 271;
            int endAngleExclusive = 360;
            SpeedWithBearingWithConfidence<Void> speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                    endAngleExclusive, useLinReg);
            return speedWithBearing;
    }
    
    private SpeedWithBearingWithConfidence<Void> estimateAnglePeakAndAverageSpeed(BoatClass boatClass, Speed windSpeed,
            int startAngleInclusive, int endAngleExclusive, boolean useLinearRegression) throws NotEnoughDataHasBeenAddedException {
        Integer[] dataCountPerAngle = getDataCountArray(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);

        
        
        double estimatedPeak = estimatePeak(boatClass, windSpeed, startAngleInclusive, endAngleExclusive, dataCountPerAngle);
        
        double convertedAngleIfOver180 = convertAngleIfNecessary(estimatedPeak);
        
        SpeedWithConfidence<Void> averagedSpeedWithConfidence = estimateSpeed(boatClass, windSpeed,
                convertedAngleIfOver180, useLinearRegression);

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
    private double calcDataCountConfidence(Integer[] dataCountPerAngle, double estimatedPeak) {
        int dataCount = dataCountPerAngle[(int) Math.round(estimatedPeak)];
        double confidence = 1 - Math.pow(Math.E, - dataCount / 150);
        return confidence;
    }

    private Integer[] getDataCountArray(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive) {
        Integer[] dataCountPerAngle = polarDataService.getDataCountsForWindSpeed(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
        return dataCountPerAngle;
    }

    private double convertAngleIfNecessary(double estimatedPeak) {
        double convertedAngleIfOver180 = estimatedPeak;
        if (estimatedPeak > 180) {
            convertedAngleIfOver180 = estimatedPeak - 360;
        }
        return convertedAngleIfOver180;
    }

    private SpeedWithConfidence<Void> estimateSpeed(BoatClass boatClass, Speed windSpeed, double estimatedPeak,
            boolean useLinearRegression) throws NotEnoughDataHasBeenAddedException {
        SpeedWithConfidence<Void> speed = polarDataService.getSpeed(boatClass, windSpeed, new DegreeBearingImpl(
                estimatedPeak), useLinearRegression);
        return speed;
    }

    private double estimatePeak(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive, Integer[] dataCountPerAngle)
            throws NotEnoughDataHasBeenAddedException {

        // Find peak by averaging the angles that have at least 50% of the max datacount
        List<Integer> dataCountList = Arrays.asList(Arrays.copyOfRange(dataCountPerAngle, startAngleInclusive,
                endAngleExclusive));
        int maxDataCount = Collections.max(dataCountList);
        if (maxDataCount < MINIMUM_DATA_COUNT_FOR_ONE_ANGLE) {
            // The angle with the most data points doesn't have sufficient data, for the polar data to have any
            // significance.
            throw new NotEnoughDataHasBeenAddedException("Only " + maxDataCount
                    + " points have been added for the angle with the most points. No significance.");
        }
        int sumOfAllUpperHalfAngles = 0;
        int numberOfAnglesAdded = 0;
        for (int i = startAngleInclusive; i < endAngleExclusive; i++) {
            Integer dataCount = dataCountPerAngle[i];
            if (dataCount > maxDataCount / 2) {
                sumOfAllUpperHalfAngles = sumOfAllUpperHalfAngles + i * dataCount;
                numberOfAnglesAdded = numberOfAnglesAdded + dataCount;
            }
        }
        
        double estimatedPeak = (double) sumOfAllUpperHalfAngles / (double) numberOfAnglesAdded;
        return estimatedPeak;
    }



}
