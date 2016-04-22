package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

public abstract class RaceLogMappingWrapper<ItemT extends WithID> {

    private final ConcurrentMap<ItemT, List<DeviceMapping<ItemT>>> mappings = new ConcurrentHashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMapping<ItemT>>> mappingsByDevice = new HashMap<>();
    
    public void forEachMapping(Consumer<DeviceMapping<ItemT>> callback) {
        forEachMapping((item, mapping) -> callback.accept(mapping));
    }
    
    public void forEachDevice(Consumer<DeviceIdentifier> callback) {
        mappingsByDevice.keySet().forEach(callback::accept);
    }
    
    public void forEachMapping(BiConsumer<ItemT, DeviceMapping<ItemT>> callback) {
        for (Map.Entry<ItemT, List<DeviceMapping<ItemT>>> entry : mappings.entrySet()) {
            ItemT item = entry.getKey();
            for (DeviceMapping<ItemT> mapping : entry.getValue()) {
                callback.accept(item, mapping);
            }
        }
    }
    
    public void forEachMappingOfDeviceIncludingTimePoint(DeviceIdentifier device, TimePoint timePoint, Consumer<DeviceMapping<ItemT>> callback) {
        List<DeviceMapping<ItemT>> mappingsForDevice = mappingsByDevice.get(device);
        if (mappingsForDevice != null) {
            for (DeviceMapping<ItemT> mapping : mappingsForDevice) {
                if (mapping.getTimeRange().includes(timePoint)) {
                    callback.accept(mapping);
                }
            }
        }
    }
    
    <FixT extends Timed> void recordFix(DeviceIdentifier device, FixT fix, BiConsumer<DeviceMapping<ItemT>, FixT> recorder) {
        if (mappingsByDevice.get(device) != null) {
            for (DeviceMapping<ItemT> mapping : mappingsByDevice.get(device)) {
                if (mapping.getTimeRange().includes(fix.getTimePoint())) {
                    recorder.accept(mapping, fix);
                }
            }
        }
    }
    
    protected abstract Map<ItemT, List<DeviceMapping<ItemT>>> calculateMappings() throws DoesNotHaveRegattaLogException;

    /**
     * Adjusts the {@link #mappings} map according to the device mappings provided from the {@link #calculateMappings()}
     * method. Afterwards, the start end end of tracking is {@link #updateStartAndEndOfTracking() updated} from the
     * mapping intervals.
     * 
     * @param loadIfNotCovered
     *            if <code>true</code>, the fixes for the mappings will be loaded based on a comparison of the previous
     *            mappings and the new mappings.
     * 
     * @throws DoesNotHaveRegattaLogException
     */
    final <FixT extends Timed, TrackT extends DynamicTrack<FixT>> void updateMappings(boolean loadIfNotCovered)
            throws DoesNotHaveRegattaLogException {
        // TODO remove fixes, if mappings have been removed
        // check if there are new time ranges not covered so far
        Map<ItemT, List<DeviceMapping<ItemT>>> newMappings = calculateMappings();
        updateMappings(newMappings, loadIfNotCovered);
    }
    
    private void updateMappings(Map<ItemT, List<DeviceMapping<ItemT>>> newMappings, boolean loadIfNotCovered) {
        // TODO lock
        if (loadIfNotCovered) {
            Set<ItemT> itemsToProcess = new HashSet<ItemT>(mappings.keySet());
            itemsToProcess.addAll(newMappings.keySet());
            for(ItemT item : itemsToProcess) {
                if(!newMappings.containsKey(item)) {
                    mappings.get(item).forEach(this::mappingRemoved);
                } else {
                    final List<DeviceMapping<ItemT>> oldMappings = mappings.containsKey(item) ? mappings.get(item) : Collections.emptyList();
                    
                    for(DeviceMapping<ItemT> newMapping : newMappings.get(item)) {
                        DeviceMapping<ItemT> oldMapping = findAndRemoveMapping(newMapping, oldMappings);
                        if (oldMapping == null) {
                            mappingAdded(newMapping);
                        } else if (newMapping.getTimeRange().equals(oldMapping.getTimeRange())) {
                            mappingChanged(oldMapping, newMapping);
                        }
                    }
                    oldMappings.forEach(this::mappingRemoved);
                }
            }
        }
        
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
    
    protected abstract void mappingRemoved(DeviceMapping<ItemT> mapping);
    protected abstract void mappingAdded(DeviceMapping<ItemT> mapping);
    protected abstract void mappingChanged(DeviceMapping<ItemT> oldMapping, DeviceMapping<ItemT> newMapping);
    
    private DeviceMapping<ItemT> findAndRemoveMapping(DeviceMapping<ItemT> mappingToFind, List<DeviceMapping<ItemT>> newItemsToProcess) {
        for (Iterator<DeviceMapping<ItemT>> iterator = newItemsToProcess.iterator(); iterator.hasNext();) {
            DeviceMapping<ItemT> deviceMapping = iterator.next();
            if(isSame(mappingToFind, deviceMapping)) {
                iterator.remove();
                return deviceMapping;
            }
        }
        return null;
    }
    
    private boolean isSame(DeviceMapping<ItemT> mapping1, DeviceMapping<ItemT> mapping2) {
        return mapping1.getDevice().equals(mapping2.getDevice()) && mapping1.getMappedTo().equals(mapping2.getMappedTo())
                && mapping1.getEventType().equals(mapping2.getEventType());
    }
}
