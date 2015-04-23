package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.TimeRange;

public enum EmptyGPSFixStore implements GPSFixStore {
    INSTANCE;

    @Override
    public void storeFix(DeviceIdentifier device, GPSFix fix) {
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            AbstractLog<?, ?> log, Competitor competitor) {
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track,
            AbstractLog<?, ?> log, Mark mark) {
    }

    @Override
    public void addListener(GPSFixReceivedListener listener, DeviceIdentifier device) {
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

    @Override
    public TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device) {
        return null;
    }

    @Override
    public long getNumberOfFixes(DeviceIdentifier device) {
        return 0;
    }
}
