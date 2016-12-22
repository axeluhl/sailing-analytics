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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Holds DeviceMappings to make it possible to track changes. This makes it possible to only process mappings that
 * weren't processed before.
 *
 * @param <ItemT> The type of items the DeviceMappings are mapped to
 */
public abstract class RegattaLogDeviceMappings<ItemT extends WithID> {
    private static final Logger logger = Logger.getLogger(RegattaLogDeviceMappings.class.getName());
    /**
     * We maintain our own collection that holds the RegattaLogs. The known RegattaLogs should by in sync with the ones that
     * can be obtained from the TrackedRace. When stopping, there could be a concurrency issue that leads to a listener
     * not being removed. This is prevented by remembering all RegattaLogs to which we attached a listener. So we can be
     * sure to not produce a memory leak.
     */
    private final Set<RegattaLog> knownRegattaLogs = new HashSet<>();

    /**
     * Lock object to be used when accessing {@link #knownRegattaLogs}.
     */
    private final NamedReentrantReadWriteLock knownRegattaLogsLock;
    
    /**
     * Lock object to be used when accessing {@link #mappings} or {@link #mappingsByDevice}.
     */
    private final NamedReentrantReadWriteLock mappingsLock;

    private final Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappings = new HashMap<>();
    private final Map<DeviceIdentifier, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappingsByDevice = new HashMap<>();
    
    private final RegattaLogEventVisitor regattaLogEventVisitor = new BaseRegattaLogEventVisitor() {
        @Override
        public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
            logger.log(Level.FINE, "New mapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappings();
        }

        @Override
        public void visit(RegattaLogDeviceMarkMappingEvent event) {
            logger.log(Level.FINE,
                    "New DeviceMarkMapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappings();
        }

        @Override
        public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
            logger.log(Level.FINE,
                    "New DeviceCompetitorMapping for: " + event.getMappedTo() + "; device: " + event.getDevice());
            updateMappings();
        }

        @Override
        public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
            logger.log(Level.FINE, "CloseOpenEndedDeviceMapping closed: " + event.getDeviceMappingEventId());
            updateMappings();
        }

        @Override
        public void visit(RegattaLogRevokeEvent event) {
            logger.log(Level.FINE, "Mapping revoked for: " + event.getRevokedEventId());
            updateMappings();
        };
    };
    
    public RegattaLogDeviceMappings(Iterable<RegattaLog> initialRegattaLogs, String raceNameForLock) {
        mappingsLock = new NamedReentrantReadWriteLock("DeviceMapping lock for race " + raceNameForLock, false);
        knownRegattaLogsLock = new NamedReentrantReadWriteLock("Lock for known RegattaLogs of race " + raceNameForLock, false);
        final boolean hasRegattaLogs;
        LockUtil.lockForWrite(knownRegattaLogsLock);
        try {
            initialRegattaLogs.forEach(this::addRegattaLogUnlocked);
            hasRegattaLogs = !knownRegattaLogs.isEmpty();
        } finally {
            LockUtil.unlockAfterWrite(knownRegattaLogsLock);
        }
        if (hasRegattaLogs) {
            updateMappings();
        }
    }
    
    public void addRegattaLog(RegattaLog regattaLog) {
        LockUtil.executeWithWriteLock(knownRegattaLogsLock, () -> addRegattaLogUnlocked(regattaLog));
        updateMappings();
    }

    private void addRegattaLogUnlocked(RegattaLog log) {
        log.addListener(regattaLogEventVisitor);
        knownRegattaLogs.add(log);
    }
    
    public void stop() {
        LockUtil.executeWithWriteLock(knownRegattaLogsLock, () -> {
            knownRegattaLogs.forEach((log) -> log.removeListener(regattaLogEventVisitor));
            knownRegattaLogs.clear();
        });
        }
    
    protected void updateMappings() {
        try {
            updateMappingsInternal();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not update device mappings", e);
        }
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
        LockUtil.executeWithReadLock(mappingsLock, () -> mappingsByDevice.keySet().forEach(callback::accept));
    }
    
    /**
     * Calls the given callback for every known mapping that's currently known.
     * 
     * @param callback the callback to call for every known mapping
     */
    public void forEachMapping(BiConsumer<ItemT, DeviceMappingWithRegattaLogEvent<ItemT>> callback) {
        LockUtil.executeWithReadLock(mappingsLock, () -> {
            for (Map.Entry<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> entry : mappings.entrySet()) {
            ItemT item = entry.getKey();
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : entry.getValue()) {
                callback.accept(item, mapping);
            }
        }
        });
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
        LockUtil.executeWithReadLock(mappingsLock, () -> {
            List<DeviceMappingWithRegattaLogEvent<ItemT>> mappingsForDevice = mappingsByDevice.get(device);
        if (mappingsForDevice != null) {
            for (DeviceMappingWithRegattaLogEvent<ItemT> mapping : mappingsForDevice) {
                if (mapping.getTimeRange().includes(timePoint)) {
                    callback.accept(mapping);
                }
            }
        }
        });
    }
    
    /**
     * @return true if there is at least one mapping for the given {@link DeviceIdentifier}, false otherwise
     */
    public boolean hasMappingForDevice(DeviceIdentifier device) {
        LockUtil.lockForRead(mappingsLock);
        try {
            return mappingsByDevice.containsKey(device);
        } finally {
            LockUtil.unlockAfterRead(mappingsLock);
        }
    }
    
    /**
     * To be implemented by subclasses to calculate the current {@link DeviceMapping}s.
     * 
     * @return All currently known DeviceMappings
     */
    protected Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> calculateMappings() {
        Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> result = new HashMap<>();
        forEachRegattaLog(
                (log) -> result.putAll(new RegattaLogDeviceMappingFinder<ItemT>(log).analyze()));
        return result;
    }

    protected void forEachRegattaLog(Consumer<RegattaLog> regattaLogConsumer) {
        LockUtil.executeWithReadLock(knownRegattaLogsLock, () -> knownRegattaLogs.forEach(regattaLogConsumer));
    }

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
    private final <FixT extends Timed, TrackT extends DynamicTrack<FixT>> void updateMappingsInternal() {
        final Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> newMappings = calculateMappings();
        final Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> oldMappings = new HashMap<>();
        LockUtil.lockForWrite(mappingsLock);
        try {
            oldMappings.putAll(mappings);
            
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
        } finally {
            LockUtil.unlockAfterWrite(mappingsLock);
        }
        calculateDiff(oldMappings, newMappings);
    }
    
    /**
     * Calculates <em>and applies</em> the mapping changes by removing listeners no longer needed for the mappings removed,
     * and by loading and adding the fixes for extended or added mappings.
     */
    private void calculateDiff(Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> previousMappings,
            Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> newMappings) {
        Set<ItemT> itemsToProcess = new HashSet<ItemT>(previousMappings.keySet());
        itemsToProcess.addAll(newMappings.keySet());
        for (ItemT item : itemsToProcess) {
            if (!newMappings.containsKey(item)) {
                previousMappings.get(item).forEach(this::mappingRemovedInternal);
            } else {
                final List<DeviceMappingWithRegattaLogEvent<ItemT>> oldMappings = previousMappings.containsKey(item)
                        ? previousMappings.get(item) : Collections.emptyList();
                final List<DeviceMappingWithRegattaLogEvent<ItemT>> addedMappings = new ArrayList<>();
                for (DeviceMappingWithRegattaLogEvent<ItemT> newMapping : newMappings.get(item)) {
                    DeviceMappingWithRegattaLogEvent<ItemT> oldMapping = findAndRemoveMapping(newMapping, oldMappings);
                    if (oldMapping == null) {
                        addedMappings.add(newMapping);
                    } else if (!newMapping.getTimeRange().equals(oldMapping.getTimeRange())) {
                        mappingChangedInternal(oldMapping, newMapping);
                    }
                }
                mappingsAddedInternal(addedMappings, item);
                oldMappings.forEach(this::mappingRemovedInternal);
            }
        }
    }
        
    /**
     * Called when a {@link DeviceMapping} was removed.
     * 
     * @param mapping the removed mapping
     */
    protected abstract void mappingRemoved(DeviceMappingWithRegattaLogEvent<ItemT> mapping);
    
    private void mappingRemovedInternal(DeviceMappingWithRegattaLogEvent<ItemT> mapping) {
        try {
            mappingRemoved(mapping);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "error while removing mapping " + mapping, e);
        }
    }

    /**
     * Called when a {@link DeviceMapping} was added.
     * 
     * @param mappings the new mapping
     */
    protected abstract void mappingsAdded(List<DeviceMappingWithRegattaLogEvent<ItemT>> mappings, ItemT item);
    
    private void mappingsAddedInternal(List<DeviceMappingWithRegattaLogEvent<ItemT>> mappings, ItemT item) {
        try {
            mappingsAdded(mappings, item);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "error while adding mapping " + mappings, e);
        }
    }

    /**
     * Called when a {@link DeviceMapping} was changed regarding its mapped time range.
     * This can occur if an open ended mapping is being closed or a close event gets revoked.
     * 
     * @param oldMapping the old mapping
     * @param newMapping the new mapping
     */
    protected abstract void mappingChanged(DeviceMappingWithRegattaLogEvent<ItemT> oldMapping,
            DeviceMappingWithRegattaLogEvent<ItemT> newMapping);
    
    private void mappingChangedInternal(DeviceMappingWithRegattaLogEvent<ItemT> oldMapping,
            DeviceMappingWithRegattaLogEvent<ItemT> newMapping) {
        try {
            mappingChanged(oldMapping, newMapping);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "error while changing mapping old: " + oldMapping + "; new: " + newMapping, e);
        }
    }
    
    private DeviceMappingWithRegattaLogEvent<ItemT> findAndRemoveMapping(
            DeviceMappingWithRegattaLogEvent<ItemT> mappingToFind,
            List<DeviceMappingWithRegattaLogEvent<ItemT>> newItemsToProcess) {
        for (Iterator<DeviceMappingWithRegattaLogEvent<ItemT>> iterator = newItemsToProcess.iterator(); iterator.hasNext();) {
            DeviceMappingWithRegattaLogEvent<ItemT> deviceMapping = iterator.next();
            if (isSame(mappingToFind, deviceMapping)) {
                iterator.remove();
                return deviceMapping;
            }
        }
        return null;
    }
    
    /**
     * Compares two device mappings based on their device, the item mapped to and the race/regatta log event type
     * that usually corresponds with the type of item to which the device is mapped. Note that in particular the
     * mappings' time ranges are ignored for this comparison.
     */
    private boolean isSame(DeviceMappingWithRegattaLogEvent<ItemT> mapping1,
            DeviceMappingWithRegattaLogEvent<ItemT> mapping2) {
        return mapping1.getDevice().equals(mapping2.getDevice()) && mapping1.getMappedTo().equals(mapping2.getMappedTo())
                && mapping1.getEventType().equals(mapping2.getEventType());
    }
}
