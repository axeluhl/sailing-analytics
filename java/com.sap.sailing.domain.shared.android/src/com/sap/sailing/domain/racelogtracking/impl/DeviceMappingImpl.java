package com.sap.sailing.domain.racelogtracking.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
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

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange) {
        this.mappedTo = mappedTo;
        this.device = device;
        this.timeRange = timeRange;
    }

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange, Serializable originalRaceLogEventId) {
        this(mappedTo, device, timeRange);
        if (originalRaceLogEventId != null) originalRaceLogEventIds.add(originalRaceLogEventId);
    }

    public DeviceMappingImpl(ItemType mappedTo, DeviceIdentifier device, TimeRange timeRange, List<? extends Serializable> originalRaceLogEventIds) {
        this(mappedTo, device, timeRange);
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
    
    public static <T extends WithID> DeviceMapping<T> convertToDeviceMapping(DeviceMappingEvent<?, T> event) {
        return new DeviceMappingImpl<T>(event.getMappedTo(), event.getDevice(), new TimeRangeImpl(event.getFrom(), event.getTo()),
                event.getId());
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
