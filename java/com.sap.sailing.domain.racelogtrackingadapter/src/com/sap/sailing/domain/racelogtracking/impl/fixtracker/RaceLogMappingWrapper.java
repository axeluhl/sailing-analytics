package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

public abstract class RaceLogMappingWrapper<ItemT extends WithID> {

    private final Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappings = new HashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappingsByDevice = new HashMap<>();
    
    private synchronized Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> getMappings() {
        return new HashMap<>(mappings);
    }
    
    private synchronized Map<DeviceIdentifier, List<DeviceMappingWithRegattaLogEvent<ItemT>>> getMappingsByDevice() {
        return new HashMap<>(mappingsByDevice);
    }

    public void forEachMapping(Consumer<DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        forEachMapping((item, mapping) -> callback.accept(mapping));
    }
    
    public void forEachDevice(Consumer<DeviceIdentifier> callback) {
        getMappingsByDevice().keySet().forEach(callback::accept);
    }
    
    public void forEachMapping(BiConsumer<ItemT, DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        for (Map.Entry<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> entry : getMappings().entrySet()) {
            ItemT item = entry.getKey();
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : entry.getValue()) {
                callback.accept(item, mapping);
            }
        }
    }
    
    public void forEachMappingOfDeviceIncludingTimePoint(DeviceIdentifier device, TimePoint timePoint,
            Consumer<DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        List<DeviceMappingWithRegattaLogEvent<ItemT>> mappingsForDevice = getMappingsByDevice().get(device);
        if (mappingsForDevice != null) {
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : mappingsForDevice) {
                if (mapping.getTimeRange().includes(timePoint)) {
                    callback.accept(mapping);
                }
            }
        }
    }
    
    protected abstract Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> calculateMappings()
            throws DoesNotHaveRegattaLogException;

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
    public final <FixT extends Timed, TrackT extends DynamicTrack<FixT>> void updateMappings(boolean loadIfNotCovered)
            throws DoesNotHaveRegattaLogException {
        // TODO remove fixes, if mappings have been removed
        // check if there are new time ranges not covered so far
        Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> newMappings = calculateMappings();
        updateMappings(newMappings, loadIfNotCovered);
    }
    
    private synchronized void updateMappings(Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> newMappings,
            boolean loadIfNotCovered) {
        if (loadIfNotCovered) {
            Set<ItemT> itemsToProcess = new HashSet<ItemT>(mappings.keySet());
            itemsToProcess.addAll(newMappings.keySet());
            for(ItemT item : itemsToProcess) {
                if(!newMappings.containsKey(item)) {
                    mappings.get(item).forEach(this::mappingRemoved);
                } else {
                    final List<DeviceMappingWithRegattaLogEvent<ItemT>> oldMappings = mappings.containsKey(item)
                            ? mappings.get(item) : Collections.emptyList();
                    
                    for (DeviceMappingWithRegattaLogEvent<ItemT> newMapping : newMappings.get(item)) {
                        DeviceMappingWithRegattaLogEvent<ItemT> oldMapping = findAndRemoveMapping(newMapping,
                                oldMappings);
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
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : newMappings.get(item)) {
                List<DeviceMappingWithRegattaLogEvent<ItemT>> list = mappingsByDevice.get(mapping.getDevice());
                if (list == null) {
                    list = new ArrayList<>();
                    mappingsByDevice.put(mapping.getDevice(), list);
                }
                list.add(mapping);
            }
        }
    }
    
    protected abstract void mappingRemoved(DeviceMappingWithRegattaLogEvent<ItemT> mapping);

    protected abstract void mappingAdded(DeviceMappingWithRegattaLogEvent<ItemT> mapping);

    protected abstract void mappingChanged(DeviceMappingWithRegattaLogEvent<ItemT> oldMapping,
            DeviceMappingWithRegattaLogEvent<ItemT> newMapping);
    
    private DeviceMappingWithRegattaLogEvent<ItemT> findAndRemoveMapping(
            DeviceMappingWithRegattaLogEvent<ItemT> mappingToFind,
            List<DeviceMappingWithRegattaLogEvent<ItemT>> newItemsToProcess) {
        for (Iterator<DeviceMappingWithRegattaLogEvent<ItemT>> iterator = newItemsToProcess.iterator(); iterator
                .hasNext();) {
            DeviceMappingWithRegattaLogEvent<ItemT> deviceMapping = iterator.next();
            if(isSame(mappingToFind, deviceMapping)) {
                iterator.remove();
                return deviceMapping;
            }
        }
        return null;
    }
    
    private boolean isSame(DeviceMappingWithRegattaLogEvent<ItemT> mapping1,
            DeviceMappingWithRegattaLogEvent<ItemT> mapping2) {
        return mapping1.getDevice().equals(mapping2.getDevice()) && mapping1.getMappedTo().equals(mapping2.getMappedTo())
                && mapping1.getEventType().equals(mapping2.getEventType());
    }
}
