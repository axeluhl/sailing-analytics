package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.TimeRangeImpl;

public class DeviceMappingImpl<ItemType extends WithID> implements DeviceMapping<ItemType> {
    private static final long serialVersionUID = -4602948402371623654L;

    private final ItemType mappedTo;
    private final DeviceIdentifier device;
    private final TimeRange timeRange;
    private final List<Serializable> originalRaceLogEventIds = new ArrayList<Serializable>();
    private final Class<?> eventType;
    
    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange, Class<?> eventType) {
        this.mappedTo = mappedTo;
        this.device = device;
        this.timeRange = timeRange;
        this.eventType = eventType;
    }

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange,
            Serializable originalRaceLogEventId, Class<?> eventType) {
        this(mappedTo, device, timeRange, eventType);
        if (originalRaceLogEventId != null) originalRaceLogEventIds.add(originalRaceLogEventId);
    }

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange,
            List<? extends Serializable> originalRaceLogEventIds, Class<?> eventType) {
        this(mappedTo, device, timeRange, eventType);
        if (originalRaceLogEventIds != null) this.originalRaceLogEventIds.addAll(originalRaceLogEventIds);
    }

    @Override
    public DeviceIdentifier getDevice() {
        return device;
    }

    @Override
    public TimeRange getTimeRange() {
        return timeRange;
    }

    @Override
    public ItemType getMappedTo() {
        return mappedTo;
    }

    @Override
    public TimePoint getTimePoint() {
        return timeRange.from();
    }
    
    @Override
    public Class<?> getEventType() {
        return eventType;
    }
    
    public static <T extends WithID> DeviceMapping<T> convertToDeviceMapping(RegattaLogDeviceMappingEvent<T> event) {
        return new DeviceMappingImpl<T>(event.getMappedTo(), event.getDevice(),
                new TimeRangeImpl(event.getFrom(), event.getToInclusive(), /* inclusive */ true),
                event.getId(), event.getClass());
    }
    
    @Override
    public String toString() {
        return "Device Mapping - device: " + device + ", mapped to: " + mappedTo + ", time range: " + timeRange;
    }
    
    @Override
    public List<Serializable> getOriginalRaceLogEventIds() {
        return originalRaceLogEventIds;
    }
}
