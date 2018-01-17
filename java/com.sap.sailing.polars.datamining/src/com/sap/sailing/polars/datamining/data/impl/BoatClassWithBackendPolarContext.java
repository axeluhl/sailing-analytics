package com.sap.sailing.polars.datamining.data.impl;

import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.datamining.data.HasBackendPolarBoatClassContext;

public class BoatClassWithBackendPolarContext implements HasBackendPolarBoatClassContext {

    private BoatClass boatClass;
    private PolarDataService polarDataService;

    public BoatClassWithBackendPolarContext(BoatClass bc, PolarDataService polarDataService) {
        this.boatClass = bc;
        this.polarDataService = polarDataService;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }
    
    public Double getTargetBeatAngle() {
        Set<SpeedWithBearingWithConfidence<Void>> set = polarDataService.getAverageTrueWindSpeedAndAngleCandidates(boatClass, new KnotSpeedImpl(15), LegType.UPWIND, Tack.STARBOARD);
        Double bestConfidence = -1.0;
        Double bestBeatAngle = null;
        for (SpeedWithBearingWithConfidence<Void> element : set) {
            if(element.getConfidence() > bestConfidence) {
                bestBeatAngle = element.getObject().getBearing().getDegrees();
                bestConfidence = element.getConfidence();
            }
        }
        return bestBeatAngle;
    }
    
    public Double getTargetRunawayAngle() {
        Set<SpeedWithBearingWithConfidence<Void>> set = polarDataService.getAverageTrueWindSpeedAndAngleCandidates(boatClass, new KnotSpeedImpl(15), LegType.DOWNWIND, Tack.PORT);
        Double bestConfidence = -1.0;
        Double bestBeatAngle = null;
        for (SpeedWithBearingWithConfidence<Void> element : set) {
            if(element.getConfidence() > bestConfidence) {
                bestBeatAngle = element.getObject().getBearing().getDegrees();
                bestConfidence = element.getConfidence();
            }
        }
        return bestBeatAngle;
    }

    @Override
    public HasBackendPolarBoatClassContext getBackendPolarBoatClassContext() {
        return this;
    }

}
