package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;
import com.sap.sailing.domain.racelog.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.impl.DeviceMappingImpl;

/**
 * Extracts the {@link DeviceMapping}s with the appropriate {@link TimeRange}s from the {@link RaceLog}.
 * Based on the {@link DeviceMappingEvent}s found in the {@code RaceLog} for the tracked {@code items}
 * ({@code Competitor}s and {@code Marks}), the actual mappings are created by removing conflicting
 * overlaps of these mappings by the rules defined below.
 * <p>
 * Mappings can be defined with an open-ended time range (e.g. track this from now on), which should then be
 * closed by a corresponding {@link CloseOpenEndedDeviceMappingEvent}.
 * <p>
 * The events in the {@code RaceLog} are ordered by their priority through the {@link RaceLogEventComparator}.
 * Therefore, the rules for resolving conflicts are applied by inspecting the events in order of increasing
 * priority one by one. Each inspected {@code DeviceMappingEvent} is resolved against the set of mappings
 * deducted so far from the events with lower priority. The resulting set of mappings from this conflict
 * resolution is then used as the new mapping basis against which the next higher-priority event is resolved.
 * <ol>
 * <li>Mappings that have no overlap in time are never in conflict.</li>
 * <li>Mappings for different items are never in conflict.</li>
 * <li>Mappings for both the same device and item with overlapping {@code TimeRanges} can be merged.</li>
 * <li>Mappings for the same item, but different devices, are in conflict if they have overlapping {@code TimeRanges}.
 * The higher-priority mapping of the two takes precedence, and overwrites the lower-priority mapping for the
 * duration of the overlap.</li>
 * </ol>
 */
public class DeviceMappingFinder<ItemT extends WithID> extends RaceLogAnalyzer<Map<ItemT, List<DeviceMapping<ItemT>>>> {
    public DeviceMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceMappingEvent;
    }

    protected DeviceMapping<ItemT> getMapping(DeviceMappingEvent<ItemT> event) {
        return new DeviceMappingImpl<ItemT>(event.getMappedTo(), event.getDevice(),
                new TimeRangeImpl(event.getFrom(), event.getTo()), event.getId());
    }

    protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
            TimePoint from, TimePoint to, List<Serializable> originalEventIds) {
        return new DeviceMappingImpl<ItemT>(item, device, new TimeRangeImpl(from, to), originalEventIds);
    }

    protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
            TimePoint from, TimePoint to, Serializable originalEventId) {
        return getMapping(device, item, from, to, Collections.singletonList(originalEventId));
    }

    protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
            TimeRange range, List<Serializable> originalEventIds) {
        return new DeviceMappingImpl<ItemT>(item, device, range, originalEventIds);
    }

    private <T, U> List<U> getItemSet(Map<T, List<U>> map, T item) {
        List<U> list = map.get(item);
        if (list == null) {
            list = new ArrayList<U>();
            map.put(item, list);
        }
        return list;
    }

    /**
     * Resolves conflicts between an existing set of mappings for one {@code item}, and a new, higher-priority mapping for the same {@code item}.
     */
    private List<DeviceMapping<ItemT>> getOverlapFreeMappings(List<DeviceMapping<ItemT>> initial,
            DeviceMapping<ItemT> toBeAdded) {
        TimeRange timeRange = toBeAdded.getTimeRange();
        ItemT item = toBeAdded.getMappedTo();
        DeviceIdentifier device = toBeAdded.getDevice();
        List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>();
        
        List<Serializable> originalEventIds = new ArrayList<Serializable>();
        originalEventIds.addAll(toBeAdded.getOriginalRaceLogEventIds());


        for (DeviceMapping<ItemT> otherMapping : initial) {
            TimeRange otherTimeRange = otherMapping.getTimeRange();

            //time ranges intersect? further investigate
            if (otherTimeRange.intersects(timeRange)) {

                //same device
                if (otherMapping.getDevice().equals(device)) {

                    //two open ranges that close each other
                    if ((otherTimeRange.openEnd() && timeRange.openBeginning() && otherTimeRange.from().before(timeRange.to())) ||
                            (timeRange.openEnd() && otherTimeRange.openBeginning() && timeRange.from().before(otherTimeRange.to()))) {
                        //shouldn't happen here any more -> dealt with in combineOpenRanges
                        //timeRange = timeRange.intersection(otherTimeRange);

                        //otherwise merge
                    } else {
                        timeRange = timeRange.union(otherTimeRange);
                        originalEventIds.addAll(otherMapping.getOriginalRaceLogEventIds());
                    }

                    //different device: higher prio overwrites other
                } else {
                    if (otherTimeRange.liesWithin(timeRange)) {
                        //just ignore the other mapping, as it is completely overwritten by the new one
                    } else {
                        if (otherTimeRange.startsBefore(timeRange)) {
                            //add the part of the lower-prio mapping, that lies before the higher-prio mapping
                            result.add(getMapping(otherMapping.getDevice(), item, otherTimeRange.from(), timeRange.from().minus(1),
                                    otherMapping.getOriginalRaceLogEventIds()));
                        }
                        if (otherTimeRange.endsAfter(timeRange)) {
                            //add the part of the lower-prio mapping, that lies after the higher-prio mapping
                            result.add(getMapping(otherMapping.getDevice(), item, timeRange.to().plus(1), otherTimeRange.to(),
                                    otherMapping.getOriginalRaceLogEventIds()));
                        }
                    }
                }

                //no intersection -> add both
            } else {
                result.add(otherMapping);
            }
        }

        //other mappings have been checked for conflicts, now add toBeAdded (time range may have grown through merges)
        result.add(getMapping(device, item, timeRange, toBeAdded.getOriginalRaceLogEventIds()));

        return result;
    }

    @Override
    protected Map<ItemT, List<DeviceMapping<ItemT>>> performAnalysis() {
        Map<ItemT, List<DeviceMappingEvent<ItemT>>> events = new HashMap<ItemT, List<DeviceMappingEvent<ItemT>>>();
        Map<ItemT, List<DeviceMapping<ItemT>>> mappings = new HashMap<ItemT, List<DeviceMapping<ItemT>>>();
        Map<Serializable, CloseOpenEndedDeviceMappingEvent> closingEvents = new HashMap<Serializable, CloseOpenEndedDeviceMappingEvent>();

        for (RaceLogEvent e : raceLog.getUnrevokedEvents()) {
            if (e instanceof DeviceMappingEvent && isValidMapping(((DeviceMappingEvent<?>) e))) {
                @SuppressWarnings("unchecked")
                DeviceMappingEvent<ItemT> mappingEvent = (DeviceMappingEvent<ItemT>) e;
                getItemSet(events, mappingEvent.getMappedTo()).add(mappingEvent);
            } else if (e instanceof CloseOpenEndedDeviceMappingEvent) {
                CloseOpenEndedDeviceMappingEvent closingEvent = (CloseOpenEndedDeviceMappingEvent) e;
                closingEvents.put(closingEvent.getDeviceMappingEventId(), closingEvent);
            }
        }

        for (ItemT item : events.keySet()) {
            List<DeviceMapping<ItemT>> closedRanges = closeOpenRanges(events.get(item), item, closingEvents);
            
            List<DeviceMapping<ItemT>> iterativelyResolved = new ArrayList<DeviceMapping<ItemT>>();
            for (DeviceMapping<ItemT> mapping : closedRanges) {
                iterativelyResolved = getOverlapFreeMappings(iterativelyResolved, mapping);
            }
            
            mappings.put(item, iterativelyResolved);
        }

        return mappings;
    }
    
    private List<DeviceMapping<ItemT>> closeOpenRanges(List<DeviceMappingEvent<ItemT>> events, ItemT item,
            Map<Serializable, CloseOpenEndedDeviceMappingEvent> closingEvents) {
        List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>();
        
        for (DeviceMappingEvent<ItemT> event : events) {            
            TimePoint from = event.getFrom();
            TimePoint to = event.getTo();
            TimePoint closingTimePoint = closingEvents.containsKey(event.getId()) ?
                    closingEvents.get(event.getId()).getClosingTimePoint() : null;
            if (from == null) from = closingTimePoint;
            if (to == null) to = closingTimePoint;
            
            result.add(getMapping(event.getDevice(), item, from, to, event.getId()));
        }
        
        return result;
    }
}
