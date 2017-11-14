package com.sap.sailing.domain.racelog.impl;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * At the moment, the timerange covered by the fixes for a device, and the number of fixes for a device are stored in a
 * metadata collection. Should be changed, see bug 1982.
 * 
 * @author Fredrik Teschke
 */
public class GPSFixStoreImpl implements GPSFixStore {
    private final SensorFixStore sensorFixStore;

    public GPSFixStoreImpl(SensorFixStore sensorFixStore) {
        this.sensorFixStore = sensorFixStore;
    }

    private <FixT extends GPSFix> void loadTrack(DynamicGPSFixTrack<?, FixT> track, DeviceIdentifier device,
            TimePoint from, TimePoint to, boolean toIsInclusive)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        loadTrack(track, device, from, to, toIsInclusive, () -> {
            return false;
        }, (c) -> {
        });
    }

    private <FixT extends GPSFix> void loadTrack(DynamicGPSFixTrack<?, FixT> track, DeviceIdentifier device,
            TimePoint from, TimePoint to, boolean toIsInclusive, BooleanSupplier isPreemptiveStopped,
            Consumer<Double> progress)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        sensorFixStore.<FixT> loadFixes(fix -> track.add(fix, /* replace */ true), device, from, to, toIsInclusive,
                isPreemptiveStopped, progress);
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RegattaLog log,
            Competitor competitor) throws NoCorrespondingServiceRegisteredException, TransformationException {
        List<DeviceMappingWithRegattaLogEvent<Competitor>> mappings = new RegattaLogDeviceCompetitorMappingFinder(log)
                .analyze().get(competitor);
        if (mappings != null) {
            for (DeviceMapping<Competitor> mapping : mappings) {
                loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(),
                        false /* toIsInclusive */);
            }
        }
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        List<DeviceMappingWithRegattaLogEvent<Mark>> mappings = new RegattaLogDeviceMarkMappingFinder(log).analyze()
                .get(mark);
        if (mappings != null) {
            for (DeviceMapping<Mark> mapping : mappings) {
                loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(),
                        false /* toIsInclusive */);
            }
        }
    }

    @Override
    public void storeFix(DeviceIdentifier device, GPSFix fix) {
        sensorFixStore.storeFix(device, fix);
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            DeviceMapping<Competitor> mapping, TimePoint start, TimePoint end, BooleanSupplier isPreemptiveStopped,
            Consumer<Double> progressReporter)
            throws TransformationException, NoCorrespondingServiceRegisteredException {

        // loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true
        // /*inclusive*/);
        final TimePoint from = Util.getLatestOfTimePoints(start, mapping.getTimeRange().from());
        final TimePoint to = Util.getEarliestOfTimePoints(end, mapping.getTimeRange().to());
        loadTrack(track, mapping.getDevice(), from, to, true /* inclusive */, isPreemptiveStopped, progressReporter);
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping, TimePoint start,
            TimePoint end, BooleanSupplier isPreemptiveStopped, Consumer<Double> progressReport)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        final TimePoint from = Util.getLatestOfTimePoints(start, mapping.getTimeRange().from());
        final TimePoint to = Util.getEarliestOfTimePoints(end, mapping.getTimeRange().to());
        loadTrack(track, mapping.getDevice(), from, to, true /* inclusive */, isPreemptiveStopped, progressReport);
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark, TimePoint start,
            TimePoint end) throws TransformationException, NoCorrespondingServiceRegisteredException {
        List<DeviceMappingWithRegattaLogEvent<Mark>> mappings = new RegattaLogDeviceMarkMappingFinder(log).analyze()
                .get(mark);
        if (mappings != null) {
            for (DeviceMapping<Mark> mapping : mappings) {
                final TimePoint from = Util.getLatestOfTimePoints(start, mapping.getTimeRange().from());
                final TimePoint to = Util.getEarliestOfTimePoints(end, mapping.getTimeRange().to());
                loadTrack(track, mapping.getDevice(), from, to, false /* toIsInclusive */);
            }
        }
    }
}
