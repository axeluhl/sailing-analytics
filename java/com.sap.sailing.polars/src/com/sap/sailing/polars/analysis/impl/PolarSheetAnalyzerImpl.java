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
    public SpeedWithBearing getOptimalUpwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 1;
        int endAngleExclusive = 90;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }

    
    @Override
    public SpeedWithBearing getOptimalDownwindSpeedWithBearingOnStarboardTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 91;
        int endAngleExclusive = 180;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }
    
    @Override
    public SpeedWithBearing getOptimalDownwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        int startAngleInclusive = 181;
        int endAngleExclusive = 270;
        SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                endAngleExclusive);
        return speedWithBearing;
    }

    @Override
    public SpeedWithBearing getOptimalUpwindSpeedWithBearingOnPortTackFor(BoatClass boatClass, Speed windSpeed)
        throws NotEnoughDataHasBeenAddedException {
            int startAngleInclusive = 271;
            int endAngleExclusive = 360;
            SpeedWithBearing speedWithBearing = estimateAnglePeakAndAverageSpeed(boatClass, windSpeed, startAngleInclusive,
                    endAngleExclusive);
            return speedWithBearing;
    }
    
    private SpeedWithBearing estimateAnglePeakAndAverageSpeed(BoatClass boatClass, Speed windSpeed,
            int startAngleInclusive, int endAngleExclusive) throws NotEnoughDataHasBeenAddedException {
        int estimatedPeak = estimatePeak(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
        
        double averagedSpeed = averageSpeed(boatClass, windSpeed, estimatedPeak);
        
        int convertedAngleIfOver180 = convertAngleIfNecessary(estimatedPeak);
        
        Bearing bearing = new DegreeBearingImpl(convertedAngleIfOver180);
        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(averagedSpeed, bearing);
        return speedWithBearing;
    }

    private int convertAngleIfNecessary(int estimatedPeak) {
        int convertedAngleIfOver180 = estimatedPeak;
        if (estimatedPeak > 180) {
            convertedAngleIfOver180 = estimatedPeak - 360;
        }
        return convertedAngleIfOver180;
    }

    private double averageSpeed(BoatClass boatClass, Speed windSpeed, int estimatedPeak)
            throws NotEnoughDataHasBeenAddedException {
        //Having the estimated peak we get the speed by averaging in a small area (3 points) around the peak
        int numberOfSpeedValuesAdded = 0;
        double sumOfSpeeds = 0;
        int numberOfExceptions = 0;
        for (int i = estimatedPeak - 1; i < estimatedPeak + 2; i++) {
            try {

                int convertedAngleIfOver180 = convertAngleIfNecessary(i);
                SpeedWithConfidence<Integer> speed = polarDataService.getSpeed(boatClass, windSpeed,
                        new DegreeBearingImpl(convertedAngleIfOver180));
                // TODO use the confidence (has to be implemented Bottom to Top through the polar pipe
                sumOfSpeeds = sumOfSpeeds + speed.getObject().getKnots();
                numberOfSpeedValuesAdded = numberOfSpeedValuesAdded + 1;
            } catch (NotEnoughDataHasBeenAddedException e) {
                if (numberOfExceptions > 0) {
                    // If more than one query throws error -> Throw error
                    throw e;
                }
                numberOfExceptions++;
            }
        }
        
        double averagedSpeed = sumOfSpeeds / numberOfSpeedValuesAdded;
        return averagedSpeed;
    }

    private int estimatePeak(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive)
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
        
        int estimatedPeak = sumOfAllUpperHalfAngles / numberOfAnglesAdded;
        return estimatedPeak;
    }



}
