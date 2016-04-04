package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.impl.DeviceMappingImpl;
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
public abstract class RegattaLogDeviceMappingFinder<ItemT extends WithID> extends RegattaLogAnalyzer<Map<ItemT, List<DeviceMapping<ItemT>>>> {
    private static Logger logger = Logger.getLogger(RegattaLogDeviceMappingFinder.class.getName());
    
    public RegattaLogDeviceMappingFinder(RegattaLog log) {
        super(log);
    }

    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceMappingEvent;
    }

    protected DeviceMapping<ItemT> createMapping(DeviceIdentifier device, ItemT item,
            TimePoint from, TimePoint to, Serializable originalEventId, Class<?> type) {
        return new DeviceMappingImpl<ItemT>(item, device, new TimeRangeImpl(from, to),
                Collections.singletonList(originalEventId), type);
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
    protected Map<ItemT, List<DeviceMapping<ItemT>>> performAnalysis() {
        Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> events = new HashMap<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>>();
        Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents = new HashMap<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent>();
        findUnrevokedMappingAndClosingEvents(events, closingEvents);
        Map<ItemT, List<DeviceMapping<ItemT>>> mappings = new HashMap<ItemT, List<DeviceMapping<ItemT>>>();
        for (ItemT item : events.keySet()) {
            mappings.put(item, closeOpenRanges(events.get(item), item, closingEvents));
        }
        return mappings;
    }

    private void findUnrevokedMappingAndClosingEvents(Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> events,
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
    
    private List<DeviceMapping<ItemT>> closeOpenRanges(List<RegattaLogDeviceMappingEvent<ItemT>> events, ItemT item,
            Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents) {
        List<DeviceMapping<ItemT>> result = new ArrayList<DeviceMapping<ItemT>>();
        for (final RegattaLogDeviceMappingEvent<ItemT> event : events) {
            TimePoint from = event.getFrom();
            TimePoint to = event.getTo();
            TimePoint closingTimePoint = closingEvents.containsKey(event.getId()) ? closingEvents.get(event.getId())
                    .getClosingTimePoint() : null;
            if (from == null) {
                logger.severe("No start time set for DeviceMappingEvent with ID: " + event.getId());
            }
            if (to == null) {
                to = closingTimePoint;
            }
            result.add(createMapping(event.getDevice(), item, from, to, event.getId(), event.getClass()));
        }
        return result;
    }

    /**
     * By revoking existing mappings for {@code item} and replacing them by mappings that exclude {@code fixTimePoint}, any
     * fix at {@code fixTimePoint} will no longer be linked to {@code item}. If a mapping is limited to the exact fix time point,
     * the mapping is only revoked and not replaced. If one boundary is exactly the fix time point, the interval is revoked and
     * replaced by a single interval with that border adjusted so it excludes the fix.
     */
    public void removeTimePointFromMapping(ItemT item, TimePoint fixTimePoint) throws NotRevokableException {
        Map<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>> events = new HashMap<ItemT, List<RegattaLogDeviceMappingEvent<ItemT>>>();
        Map<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents = new HashMap<Serializable, RegattaLogCloseOpenEndedDeviceMappingEvent>();
        findUnrevokedMappingAndClosingEvents(events, closingEvents);
        for (final RegattaLogDeviceMappingEvent<ItemT> event : events.get(item)) {
            final TimePoint from = event.getFrom();
            final RegattaLogCloseOpenEndedDeviceMappingEvent closingEvent = closingEvents.get(event.getId());
            final TimePoint to = closingEvent!= null ? closingEvent.getClosingTimePoint() : event.getTo();
            final TimeRange mappingTimeRange = new TimeRangeImpl(from, to);
            if (mappingTimeRange.includes(fixTimePoint)) {
                if (closingEvent != null) {
                    log.revokeEvent(closingEvent.getAuthor(), closingEvent, "removing single time point "+fixTimePoint+" from mapping for "+item);
                }
                log.revokeEvent(event.getAuthor(), event, "removing single time point "+fixTimePoint+" from mapping for "+item);
                final TimePoint endOfFirstHalf = fixTimePoint.minus(1);
                final TimePoint startOfSecondHalf = fixTimePoint.plus(1);
                if (!endOfFirstHalf.before(from)) {
                    log.add(createDeviceMappingEvent(item, event.getAuthor(), from, endOfFirstHalf, event.getDevice()));
                }
                if (!to.before(startOfSecondHalf)) {
                    log.add(createDeviceMappingEvent(item, event.getAuthor(), startOfSecondHalf, to, event.getDevice()));
                }
            }
        }
    }

    protected abstract RegattaLogDeviceMappingEvent<ItemT> createDeviceMappingEvent(ItemT item, AbstractLogEventAuthor author, TimePoint plus, TimePoint to, DeviceIdentifier deviceId);
}
