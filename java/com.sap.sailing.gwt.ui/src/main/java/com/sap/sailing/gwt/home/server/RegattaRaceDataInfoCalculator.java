package com.sap.sailing.gwt.home.server;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;

public class RegattaRaceDataInfoCalculator implements EventActionUtil.RaceCallback {
    
    private boolean hasGPSData, hasWindData, hasVideoData, hasAudioData;

    @Override
    public void doForRace(RaceContext context) {
        RaceDataInfo raceDataInfo = context.getRaceDataInfo();
        hasGPSData |= raceDataInfo.hasGPSData();
        hasWindData |= raceDataInfo.hasWindData();
        hasVideoData |= raceDataInfo.hasVideoData();
        hasAudioData |= raceDataInfo.hasAudioData();
    }
    
    public RaceDataInfo getRaceDataInfo() {
        return new RaceDataInfo(hasGPSData, hasWindData, hasVideoData, hasAudioData);
    }

}
