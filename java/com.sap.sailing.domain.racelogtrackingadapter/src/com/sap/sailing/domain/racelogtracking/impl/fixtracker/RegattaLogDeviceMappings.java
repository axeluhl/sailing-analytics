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
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

/**
 * Holds DeviceMappings to make it possible to track changes. This makes it possible to only process mappings that
 * weren't processed before.
 *
 * @param <ItemT> The type of items the DeviceMappings are mapped to
 */
public abstract class RegattaLogDeviceMappings<ItemT extends WithID> {

    private final Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappings = new HashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappingsByDevice = new HashMap<>();
    
    /**
     * Used internally to access {@link #mappings} with a defensive copy to not run into concurrency issues if this is
     * called while an update is done.
     * 
     * @return the mappings
     */
    private synchronized Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> getDeviceMappings() {
        return new HashMap<>(mappings);
    }
    
    /**
     * Used internally to access {@link #mappingsByDevice} with a defensive copy to not run into concurrency issues if
     * this is called while an update is done.
     * 
     * @return the mappings
     */
    private synchronized Map<DeviceIdentifier, List<DeviceMappingWithRegattaLogEvent<ItemT>>> getMappingsByDevice() {
        return new HashMap<>(mappingsByDevice);
    }

    /**
     * Calls the given callback for every known mapping that's currently known.
     * 
     * @param callback the callback to call for every known mapping
     */
    public void forEachMapping(Consumer<DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        forEachMapping((item, mapping) -> callback.accept(mapping));
    }
    
    /**
     * Calls the given callback for every device mapped by at least one of the known mappings.
     * 
     * @param callback the callback to call for every mapped device
     */
    public void forEachDevice(Consumer<DeviceIdentifier> callback) {
        getMappingsByDevice().keySet().forEach(callback::accept);
    }
    
    /**
     * Calls the given callback for every known mapping that's currently known.
     * 
     * @param callback the callback to call for every known mapping
     */
    public void forEachMapping(BiConsumer<ItemT, DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        for (Map.Entry<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> entry : getDeviceMappings().entrySet()) {
            ItemT item = entry.getKey();
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : entry.getValue()) {
                callback.accept(item, mapping);
            }
        }
    }
    
    /**
     * Calls the given callback for every DeviceMapping that is known for the given {@link DeviceIdentifier} that
     * includes the given {@link TimePoint}.
     * 
     * @param device
     *            the device to get the mappings for
     * @param timePoint
     *            the TimePoint to check DeviceMappings for
     * @param callback
     *            the callback to call for every known mapping of the given {@link DeviceIdentifier} that includes the
     *            given {@link TimePoint}.
     */
    public void forEachMappingOfDeviceIncludingTimePoint(DeviceIdentifier device, TimePoint timePoint,
            Consumer<DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        List<DeviceMappingWithRegattaLogEvent<ItemT>> mappingsForDevice;
        synchronized (this) {
            mappingsForDevice = mappingsByDevice.get(device);
        }
        if (mappingsForDevice != null) {
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : mappingsForDevice) {
                if (mapping.getTimeRange().includes(timePoint)) {
                    callback.accept(mapping);
                }
            }
        }
    }
    
    /**
     * To be implemented by subclasses to calculate the current {@link DeviceMapping}s.
     * 
     * @return All currently known DeviceMappings
     */
    protected abstract Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> calculateMappings();

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
    public final <FixT extends Timed, TrackT extends DynamicTrack<FixT>> void updateMappings(boolean loadIfNotCovered) {
        // TODO remove fixes, if mappings have been removed
        // check if there are new time ranges not covered so far
        Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> newMappings = calculateMappings();
        updateMappings(newMappings, loadIfNotCovered);
    }
    
    /**
     * Used internally to update the internal set of mappings to the state of the new mappings.
     * This ensures that the internal state is updated and all new and changed mappings are correctly processed.
     * 
     * @param newMappings the new mappings
     * @param loadIfNotCovered true to inform about new/changed/removed mappings
     */
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
    
    /**
     * Called when a {@link DeviceMapping} was removed.
     * 
     * @param mapping the removed mapping
     */
    protected abstract void mappingRemoved(DeviceMappingWithRegattaLogEvent<ItemT> mapping);

    /**
     * Called when a {@link DeviceMapping} was added.
     * 
     * @param mapping the new mapping
     */
    protected abstract void mappingAdded(DeviceMappingWithRegattaLogEvent<ItemT> mapping);

    /**
     * Called when a {@link DeviceMapping} was changed regarding its mapped time range.
     * This can occur if an open ended mapping is being closed or a close event gets revoked.
     * 
     * @param oldMapping the old mapping
     * @param newMapping the new mapping
     */
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
