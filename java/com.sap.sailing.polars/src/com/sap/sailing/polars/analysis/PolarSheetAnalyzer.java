package com.sap.sailing.polars.analysis;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.polars.PolarDataService;

/**
 * Extracts typical measures from polar sheets.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarSheetAnalyzer {

    private PolarDataService polarDataService;

    public PolarSheetAnalyzer(PolarDataService polarDataService) {
        this.polarDataService = polarDataService;
    }

    public SpeedWithBearing getOptimalUpwindSpeedWithBearingFor(BoatClass boatClass, Speed windSpeed) {
        PolarSheetsData data = polarDataService.getPolarSheetForBoatClass(boatClass);
        Number[][] dataPerWindSpeed = data.getAveragedPolarDataByWindSpeed();
        int windSpeedIndex = data.getStepping().getLevelIndexForValue(windSpeed.getKnots());
        Number[] dataForProvidedWindSpeed = dataPerWindSpeed[windSpeedIndex];
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

}
