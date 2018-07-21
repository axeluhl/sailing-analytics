package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.racelogtracking.impl.DeviceMappingWithRegattaLogEventImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.TimeRangeImpl;

/**
 * Extracts the {@link DeviceMapping}s with the appropriate {@link TimeRange}s from the {@link RaceLog}.
 * Based on the {@link RegattaLogDeviceMappingEvent}s found in the {@code RaceLog} for the tracked {@code items}
 * ({@code Competitor}s and {@code Marks}), the actual mappings are created.
 * <p>
 * Mappings can be defined with an open-ended time range (e.g. track this from now on), which should then be
 * closed by a corresponding {@link RegattaLogCloseOpenEndedDeviceMappingEvent}.
 * <p>
 * The process of removing conflicts between mappings to ensure that only one mapping was active for
 * one tracked item has been removed. Instead, every {@code DeviceMappingEvent} now results directly in one
 * {@code DeviceMapping}, the only changes being introduced by closing open time ranges in cases where an
 * according {@code CloseOpenEndedDeviceMappingEvent} exists.
 */
public class RegattaLogDeviceMappingFinder<ItemT extends WithID>
        extends RegattaLogAnalyzer<Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>>> {
    private static Logger logger = Logger.getLogger(RegattaLogDeviceMappingFinder.class.getName());
    
    public RegattaLogDeviceMappingFinder(RegattaLog log) {
        super(log);
    }

    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceMappingEvent;
    }

    protected DeviceMappingWithRegattaLogEvent<ItemT> createMapping(DeviceIdentifier device, ItemT item,
            TimePoint from, TimePoint toInclusive, Serializable originalEventId, RegattaLogDeviceMappingEvent<ItemT> event) {
        return new DeviceMappingWithRegattaLogEventImpl<ItemT>(item, device, new TimeRangeImpl(from, toInclusive, /* inclusive */ true),
                Collections.singletonList(originalEventId), event);
    }

    private List<RegattaLogDeviceMappingEvent<ItemT>> getMappingEventsForItem(Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> map, ItemT item) {
        List<RegattaLogDeviceMappingEvent<ItemT>> list = map.get(item);
        if (list == null) {
            list = new ArrayList<RegattaLogDeviceMappingEvent<ItemT>>();
            map.put(item, list);
        }
        return list;
    }
    
    @Override
    protected Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> performAnalysis() {
        Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> events = new HashMap<>();
        Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents = new HashMap<>();
        findUnrevokedMappingAndClosingEvents(events, closingEvents);
        Map<ItemT, List<DeviceMappingWithRegattaLogEvent<ItemT>>> mappings = new HashMap<>();
        for (ItemT item : events.keySet()) {
            mappings.put(item, closeOpenRanges(events.get(item), item, closingEvents));
        }
        return mappings;
    }

    protected void findUnrevokedMappingAndClosingEvents(Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> events,
            Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents) {
        for (RegattaLogEvent e : getLog().getUnrevokedEvents()) {
            if (e instanceof RegattaLogDeviceMappingEvent && isValidMapping(((RegattaLogDeviceMappingEvent<?>) e))) {
                @SuppressWarnings("unchecked")
                RegattaLogDeviceMappingEvent<ItemT> mappingEvent = (RegattaLogDeviceMappingEvent<ItemT>) e;
                getMappingEventsForItem(events, mappingEvent.getMappedTo()).add(mappingEvent);
            } else if (e instanceof RegattaLogCloseOpenEndedDeviceMappingEvent) {
                //a higher priority closing events for the same mapping event overwrites the lower priority one
                RegattaLogCloseOpenEndedDeviceMappingEvent closingEvent = (RegattaLogCloseOpenEndedDeviceMappingEvent) e;
                closingEvents.put(closingEvent.getDeviceMappingEventId(), closingEvent);
            }
        }
    }
    
    private List<DeviceMappingWithRegattaLogEvent<ItemT>> closeOpenRanges(
            List<RegattaLogDeviceMappingEvent<ItemT>> events, ItemT item,
            Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents) {
        List<DeviceMappingWithRegattaLogEvent<ItemT>> result = new ArrayList<>();
        for (final RegattaLogDeviceMappingEvent<ItemT> event : events) {
            TimePoint from = event.getFrom();
            TimePoint toInclusive = event.getToInclusive();
            TimePoint closingTimePointInclusive = closingEvents.containsKey(event.getId()) ? closingEvents.get(event.getId())
                    .getClosingTimePointInclusive() : null;
            if (from == null) {
                logger.severe("No start time set for DeviceMappingEvent with ID: " + event.getId());
            }
            if (toInclusive == null) {
                toInclusive = closingTimePointInclusive;
            }
            result.add(createMapping(event.getDevice(), item, from, toInclusive, event.getId(), event));
        }
        return result;
    }

}
