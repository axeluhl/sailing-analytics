package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.racelog.tracking.GPSFixReceivedListener;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

public class RaceLogMappingWrapper<ItemT extends WithID> {
    private static final Logger logger = Logger.getLogger(RaceLogMappingWrapper.class.getName());

    private final ConcurrentMap<ItemT, List<DeviceMapping<ItemT>>> mappings = new ConcurrentHashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> mappingsByDevice = new HashMap<>();
    
    private final RegattaLog regattaLog;

    public RaceLogMappingWrapper(RegattaLog regattaLog) {
        this.regattaLog = regattaLog;
    }
    
    public <FixT extends Timed, TrackT extends DynamicTrack<FixT>> void updateMappings(
            Function<RegattaLog, RegattaLogDeviceMappingFinder<ItemT>> mappingFinder, boolean loadIfNotCovered,
            Function<ItemT, TrackT> trackFactory, BiConsumer<TrackT, DeviceMapping<ItemT>> trackLoader)
            throws DoesNotHaveRegattaLogException {
        // TODO remove fixes, if mappings have been removed
        // check if there are new time ranges not covered so far
        Map<ItemT, List<DeviceMapping<ItemT>>> newMappings = mappingFinder.apply(regattaLog).analyze();
        if (loadIfNotCovered) {
            for (ItemT item : newMappings.keySet()) {
                TrackT track = trackFactory.apply(item);
                List<DeviceMapping<ItemT>> oldMappings = mappings.get(item);
                if (oldMappings != null) {
                    for (DeviceMapping<ItemT> newMapping : newMappings.get(item)) {
                        if (!hasMappingAlreadyBeenLoaded(newMapping, oldMappings)) {
                            try {
                                trackLoader.accept(track, newMapping);
                            } catch (NoCorrespondingServiceRegisteredException exc) {
                                logger.log(Level.WARNING, "Could not load track " + newMapping.getMappedTo());
                            }
                        }
                    }
                }
            }
        }
        updateMappings(newMappings);
    }
    
    public <FixT extends Timed> void addListeners(final BiConsumer<ItemT, FixT> recorder,
            BiConsumer<GPSFixReceivedListener<FixT>, DeviceIdentifier> listenerConsumer) {
        for (List<DeviceMapping<ItemT>> list : mappings.values()) {
            for (DeviceMapping<ItemT> mapping : list) {
                listenerConsumer.accept((device, fix) -> recordFix(device, fix, recorder), mapping.getDevice());
            }
        }
    }
    
    private <FixT extends Timed> void recordFix(DeviceIdentifier device, FixT fix, BiConsumer<ItemT, FixT> recorder) {
        if (mappingsByDevice.get(device) != null) {
            for (DeviceMapping<ItemT> mapping : mappingsByDevice.get(device)) {
                ItemT item = mapping.getMappedTo();
                if (mapping.getTimeRange().includes(fix.getTimePoint())) {
                    recorder.accept(item, fix);
                }
            }
        }
    }
    
    private boolean hasMappingAlreadyBeenLoaded(DeviceMapping<ItemT> newMapping,
            List<DeviceMapping<ItemT>> oldMappings) {
        if (oldMappings != null) {
            for (DeviceMapping<ItemT> oldMapping : oldMappings) {
                DeviceIdentifier oldDevice = oldMapping.getDevice(), newDevice = newMapping.getDevice();
                TimeRange oldTimeRange = oldMapping.getTimeRange(), newTimeRange = newMapping.getTimeRange();
                if (newDevice == oldDevice && newTimeRange.liesWithin(oldTimeRange)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void updateMappings(Map<ItemT, List<DeviceMapping<ItemT>>> newMappings) {
        mappings.clear();
        mappings.putAll(newMappings);
        mappingsByDevice.clear();
        for (ItemT item : newMappings.keySet()) {
            for (DeviceMapping<ItemT> mapping : newMappings.get(item)) {
                List<DeviceMapping<ItemT>> list = mappingsByDevice.get(mapping.getDevice());
                if (list == null) {
                    list = new ArrayList<>();
                    mappingsByDevice.put(mapping.getDevice(), list);
                }
                list.add(mapping);
            }
        }
    }

}
