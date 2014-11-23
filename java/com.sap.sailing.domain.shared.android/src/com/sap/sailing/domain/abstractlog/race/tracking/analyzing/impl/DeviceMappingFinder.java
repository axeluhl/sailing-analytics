package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMapping;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Extracts the {@link DeviceMapping}s with the appropriate {@link TimeRange}s from the {@link RaceLog}.
 * Based on the {@link DeviceMappingEvent}s found in the {@code RaceLog} for the tracked {@code items}
 * ({@code Competitor}s and {@code Marks}), the actual mappings are created.
 * <p>
 * Mappings can be defined with an open-ended time range (e.g. track this from now on), which should then be
 * closed by a corresponding {@link CloseOpenEndedDeviceMappingEvent}.
 * <p>
 * The process of removing conflicts between mappings to ensure that only one mapping was active for
 * one tracked item has been removed. Instead, every {@code DeviceMappingEvent} now results directly in one
 * {@code DeviceMapping}, the only changes being introduced by closing open time ranges in cases where an
 * according {@code CloseOpenEndedDeviceMappingEvent} exists.
 */
public class DeviceMappingFinder<ItemT extends WithID> extends RaceLogAnalyzer<Map<ItemT, List<DeviceMapping<ItemT>>>> {
    private static Logger logger = Logger.getLogger(DeviceMappingFinder.class.getName());
    
    public DeviceMappingFinder(RaceLog raceLog) {
        super(raceLog);
    }

    protected boolean isValidMapping(DeviceMappingEvent<?> mapping) {
        return mapping instanceof DeviceMappingEvent;
    }

    protected DeviceMapping<ItemT> getMapping(DeviceIdentifier device, ItemT item,
            TimePoint from, TimePoint to, Serializable originalEventId) {
        return new DeviceMappingImpl<ItemT>(item, device, new TimeRangeImpl(from, to),
                Collections.singletonList(originalEventId));
    }

    private <T, U> List<U> getItemSet(Map<T, List<U>> map, T item) {
        List<U> list = map.get(item);
        if (list == null) {
            list = new ArrayList<U>();
            map.put(item, list);
        }
        return list;
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
                //a higher priority closing events for the same mapping event overwrites the lower priority one
                CloseOpenEndedDeviceMappingEvent closingEvent = (CloseOpenEndedDeviceMappingEvent) e;
                closingEvents.put(closingEvent.getDeviceMappingEventId(), closingEvent);
            }
        }

        for (ItemT item : events.keySet()) {
            mappings.put(item, closeOpenRanges(events.get(item), item, closingEvents));
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
            if (from == null) logger.severe("No start time set for DeviceMappingEvent with ID: "+event.getId());
            if (to == null) to = closingTimePoint;
            
            result.add(getMapping(event.getDevice(), item, from, to, event.getId()));
        }
        
        return result;
    }
}
