package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public enum EmptyGPSFixStore implements GPSFixStore {
    INSTANCE;

    @Override
    public void storeFix(DeviceIdentifier device, GPSFix fix) {
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            RaceLog raceLog, Competitor competitor) {
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track,
            RaceLog raceLog, Mark mark) {
    }

    @Override
    public void addListener(GPSFixReceivedListener listener) {
    }

    @Override
    public void removeListener(GPSFixReceivedListener listener) {
    }

    @Override
    public void loadCompetitorTrack(
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            DeviceMapping<Competitor> mapping) {

    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track,
            DeviceMapping<Mark> mapping) {

    }
}
