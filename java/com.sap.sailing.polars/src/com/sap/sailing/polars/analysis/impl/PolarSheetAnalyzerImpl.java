package com.sap.sailing.polars.analysis.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetsData;
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

    private static final int MINIMUM_DATA_COUNT_FOR_ONE_ANGLE = 10;
    private PolarDataService polarDataService;

    public PolarSheetAnalyzerImpl(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    @Override
    public SpeedWithBearing getAverageUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 1;
        int endAngleExclusive = 90;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }

    
    @Override
    public SpeedWithBearing getAverageDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 91;
        int endAngleExclusive = 180;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }
    
    @Override
    public SpeedWithBearing getAverageDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 181;
        int endAngleExclusive = 270;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }

    @Override
    public SpeedWithBearing getAverageUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
        throws NotEnoughDataHasBeenAddedException {
            int startAngleInclusive = 271;
            int endAngleExclusive = 360;
            SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                    endAngleExclusive);
            return speedWithBearing;
    }
    
    private SpeedWithBearing estimateAnglePeakAndAverageSpeed(BoatClass boatClass, Speed windSpeed,
            int startAngleInclusive, int endAngleExclusive) throws NotEnoughDataHasBeenAddedException {
        double estimatedPeak = estimatePeak(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
        
        double convertedAngleIfOver180 = convertAngleIfNecessary(estimatedPeak);
        
        double averagedSpeed = estimatedSpeed(boatClass, windSpeed, convertedAngleIfOver180);
        
        
        
        Bearing bearing = new DegreeBearingImpl(convertedAngleIfOver180);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(averagedSpeed, bearing);
        return speedWithBearing;
    }

    private double convertAngleIfNecessary(double estimatedPeak) {
        double convertedAngleIfOver180 = estimatedPeak;
        if (estimatedPeak > 180) {
            convertedAngleIfOver180 = estimatedPeak - 360;
        }
        return convertedAngleIfOver180;
    }

    private double estimatedSpeed(BoatClass boatClass, Speed windSpeed, double estimatedPeak)
            throws NotEnoughDataHasBeenAddedException {
        SpeedWithConfidence<Integer> speed = polarDataService.getSpeed(boatClass, windSpeed, new DegreeBearingImpl(
                estimatedPeak));
        // TODO use the confidence (has to be implemented Bottom to Top through the polar pipe

        return speed.getObject().getKnots();
    }

    private double estimatePeak(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive)
            throws NotEnoughDataHasBeenAddedException {
        PolarSheetsData sheet = polarDataService.getPolarSheetForBoatClass(boatClass);
        int windIndex = sheet.getStepping().getLevelIndexForValue(windSpeed.getKnots());
        Integer[] dataCountPerAngle = sheet.getDataCountPerAngleForWindspeed(windIndex);

        // Find peak by averaging the angles that have at least 50% of the max datacount
        List<Integer> dataCountList = Arrays.asList(Arrays.copyOfRange(dataCountPerAngle, startAngleInclusive,
                endAngleExclusive));
        int maxDataCount = Collections.max(dataCountList);
        if (maxDataCount < MINIMUM_DATA_COUNT_FOR_ONE_ANGLE) {
            // The angle with the most data points doesn't have sufficient data, for the polar data to have any
            // significance.
            throw new NotEnoughDataHasBeenAddedException();
        }
        int sumOfAllUpperHalfAngles = 0;
        int numberOfAnglesAdded = 0;
        for (int i = startAngleInclusive; i < endAngleExclusive; i++) {
            if (dataCountPerAngle[i] > maxDataCount / 2) {
                sumOfAllUpperHalfAngles = sumOfAllUpperHalfAngles + i;
                numberOfAnglesAdded = numberOfAnglesAdded + 1;
            }
        }
        
        double estimatedPeak = (double) sumOfAllUpperHalfAngles / (double) numberOfAnglesAdded;
        return estimatedPeak;
    }



}
