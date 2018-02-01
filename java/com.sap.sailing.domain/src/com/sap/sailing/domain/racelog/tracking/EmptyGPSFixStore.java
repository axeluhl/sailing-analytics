package com.sap.sailing.domain.racelog.tracking;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;

public enum EmptyGPSFixStore implements GPSFixStore {
    INSTANCE;

    @Override
    public void storeFix(DeviceIdentifier device, GPSFix fix) {
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            RegattaLog log, Competitor competitor) {
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track,
            RegattaLog log, Mark mark) {
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark,
            TimePoint start, TimePoint end) throws TransformationException, NoCorrespondingServiceRegisteredException {
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            DeviceMapping<Competitor> mapping, TimePoint start, TimePoint end, BooleanSupplier isPreemptiveStopped,
            Consumer<Double> progressReporter)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping, TimePoint start,
            TimePoint end, BooleanSupplier isPreemptiveStopped, Consumer<Double> progressReporter)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        
    }
}
