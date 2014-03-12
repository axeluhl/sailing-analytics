package com.sap.sailing.polars.analysis.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.analysis.PolarSheetAnalyzer;

/**
 * Extracts typical measures from polar sheets.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarSheetAnalyzerImpl implements PolarSheetAnalyzer {

    private PolarDataService polarDataService;

    public PolarSheetAnalyzerImpl(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    @Override
    public SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        PolarSheetsData data = polarDataService.getPolarSheetForBoatClass(boatClass);
        Number[][] dataPerWindSpeed = data.getAveragedPolarDataByWindSpeed();
        int windSpeedIndex = data.getStepping().getLevelIndexForValue(windSpeed.getKnots());
        Number[] dataForProvidedWindSpeed = dataPerWindSpeed[windSpeedIndex];
        if (dataForProvidedWindSpeed.length == 360) {
            dataForProvidedWindSpeed = convertToOneSidedPolar(dataForProvidedWindSpeed);
        }
        double optUpwindSpeed = 0;
        int optUpwindDeg = 0;
        for (int i = 0; i < 90; i++) {
            Number speed = dataForProvidedWindSpeed[i];
            double speedUpwind = speed.doubleValue() * Math.cos(Math.toRadians(i));
            if (speedUpwind > optUpwindSpeed) {
                optUpwindDeg = i;
                optUpwindSpeed = speedUpwind;
            }
        }
        return new KnotSpeedWithBearingImpl(optUpwindSpeed, new DegreeBearingImpl(optUpwindDeg));
    }

    @Override
    public SpeedWithBearing getOptimalDownwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        PolarSheetsData data = polarDataService.getPolarSheetForBoatClass(boatClass);
        Number[][] dataPerWindSpeed = data.getAveragedPolarDataByWindSpeed();
        int windSpeedIndex = data.getStepping().getLevelIndexForValue(windSpeed.getKnots());
        Number[] dataForProvidedWindSpeed = dataPerWindSpeed[windSpeedIndex];
        double optDownwindSpeed = 0;
        int optUpwindDeg = 0;
        for (int i = 90; i < 180; i++) {
            Number speed = dataForProvidedWindSpeed[i];
            double speedUpwind = speed.doubleValue() * Math.sin(Math.toRadians(i - 90));
            if (speedUpwind > optDownwindSpeed) {
                optUpwindDeg = i;
                optDownwindSpeed = speedUpwind;
            }
        }
        return new KnotSpeedWithBearingImpl(optDownwindSpeed, new DegreeBearingImpl(optUpwindDeg));
    }

    private Number[] convertToOneSidedPolar(Number[] dataForProvidedWindSpeed) {
        Number[] oneSided = new Number[180];
        for (int i = 181; i < 360; i++) {
            oneSided[360 - i] = (dataForProvidedWindSpeed[360 - i].doubleValue() + dataForProvidedWindSpeed[i]
                    .doubleValue()) / 2;
        }
        return oneSided;
    }

}
