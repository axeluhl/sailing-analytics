package com.sap.sailing.domain.racelog.impl;

import java.util.List;
import java.util.function.Consumer;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.WithID;

/**
 * At the moment, the timerange covered by the fixes for a device, and the number of
 * fixes for a device are stored in a metadata collection. Should be changed, see bug 1982.
 * @author Fredrik Teschke
 *
 */
public class GPSFixStoreImpl implements GPSFixStore {
    private final SensorFixStore sensorFixStore;

    public GPSFixStoreImpl(SensorFixStore sensorFixStore) {
        this.sensorFixStore = sensorFixStore;
    }

    private <FixT extends GPSFix> void loadTrack(DynamicGPSFixTrack<?, FixT> track, DeviceIdentifier device,
            TimePoint from, TimePoint to, boolean inclusive) throws NoCorrespondingServiceRegisteredException,
            TransformationException {
        sensorFixStore.<FixT>loadFixes(track::add, device, from, to, inclusive);
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RegattaLog log, Competitor competitor)
    throws NoCorrespondingServiceRegisteredException, TransformationException{
        List<DeviceMapping<Competitor>> mappings = new RegattaLogDeviceCompetitorMappingFinder(log).analyze().get(competitor);
        if (mappings != null) {
            for (DeviceMapping<Competitor> mapping : mappings) {
                loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
            }
        }
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark)
    throws NoCorrespondingServiceRegisteredException, TransformationException{
        List<DeviceMapping<Mark>> mappings = new RegattaLogDeviceMarkMappingFinder(log).analyze().get(mark);
        if (mappings != null) {
            for (DeviceMapping<Mark> mapping : mappings) {
                loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
            }
        }
    }

    @Override
    public void storeFix(DeviceIdentifier device, GPSFix fix) {
        sensorFixStore.storeFix(device, fix);
    }

    @Override
    public synchronized void addListener(FixReceivedListener<GPSFix> listener, DeviceIdentifier device) {
        sensorFixStore.addListener(listener, device);
    }

    @Override
    public synchronized void removeListener(FixReceivedListener<GPSFix> listener) {
        sensorFixStore.removeListener(listener);
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, DeviceMapping<Competitor> mapping)
    throws TransformationException, NoCorrespondingServiceRegisteredException {
        loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
    }

    @Override
    public void loadCompetitorTrack(DynamicGPSFixTrack<Competitor, GPSFixMoving> track, RegattaLog log,
            Competitor competitor, TimePoint start, TimePoint end) throws TransformationException {
        List<DeviceMapping<Competitor>> mappings = new RegattaLogDeviceCompetitorMappingFinder(log).analyze().get(competitor);
        if (mappings != null) {
            for (DeviceMapping<Competitor> mapping : mappings) {
                final TimePoint from = Util.getLatestOfTimePoints(start, mapping.getTimeRange().from());
                final TimePoint to = Util.getEarliestOfTimePoints(end, mapping.getTimeRange().to());
                loadTrack(track, mapping.getDevice(), from, to, true /*inclusive*/);
            }
        }
    }

    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, DeviceMapping<Mark> mapping)
    throws TransformationException, NoCorrespondingServiceRegisteredException {
        loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true /*inclusive*/);
    }
    
    @Override
    public void loadMarkTrack(DynamicGPSFixTrack<Mark, GPSFix> track, RegattaLog log, Mark mark,
            TimePoint start, TimePoint end) throws TransformationException, NoCorrespondingServiceRegisteredException {
        List<DeviceMapping<Mark>> mappings = new RegattaLogDeviceMarkMappingFinder(log).analyze().get(mark);
        if (mappings != null) {
            for (DeviceMapping<Mark> mapping : mappings) {
                final TimePoint from = Util.getLatestOfTimePoints(start, mapping.getTimeRange().from());
                final TimePoint to = Util.getEarliestOfTimePoints(end, mapping.getTimeRange().to());
                loadTrack(track, mapping.getDevice(), from, to, true /*inclusive*/);
            }
        }
    }

    @Override
    public void loadTrack(DynamicGPSFixTrack<WithID, ?> track, DeviceMapping<WithID> mapping)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        loadTrack(track, mapping.getDevice(), mapping.getTimeRange().from(), mapping.getTimeRange().to(), true);
    }
    
    @Override
    public TimeRange getTimeRangeCoveredByFixes(DeviceIdentifier device)
            throws TransformationException, NoCorrespondingServiceRegisteredException {
        return sensorFixStore.getTimeRangeCoveredByFixes(device);
    }
    
    @Override
    public long getNumberOfFixes(DeviceIdentifier device) throws TransformationException, NoCorrespondingServiceRegisteredException {
        return sensorFixStore.getNumberOfFixes(device);
    }
    
    @Override
    public <FixT extends GPSFix> void loadFixes(Consumer<FixT> consumer, DeviceIdentifier deviceIdentifier,
            TimePoint start, TimePoint end, boolean inclusive)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        sensorFixStore.loadFixes(consumer, deviceIdentifier, start, end, inclusive);
    }
}
