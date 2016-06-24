package com.sap.sailing.gwt.home.server;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;

/**
 * {@link RaceCallback} implementation, which calculates the {@link RaceDataInfo} for a regatta by aggregating the
 * information of the {@link RaceContext races} passed to the {@link #doForRace(RaceContext)} method.
 */
public class RegattaRaceDataInfoCalculator implements RaceCallback {

    private boolean hasGPSData, hasWindData, hasVideoData, hasAudioData;

    @Override
    public void doForRace(RaceContext context) {
        RaceDataInfo raceDataInfo = context.getRaceDataInfo();
        hasGPSData |= raceDataInfo.hasGPSData();
        hasWindData |= raceDataInfo.hasWindData();
        hasVideoData |= raceDataInfo.hasVideoData();
        hasAudioData |= raceDataInfo.hasAudioData();
    }

    /**
     * @return the aggregated {@link RaceDataInfo}
     */
    public RaceDataInfo getRaceDataInfo() {
        return new RaceDataInfo(hasGPSData, hasWindData, hasVideoData, hasAudioData);
    }

}
