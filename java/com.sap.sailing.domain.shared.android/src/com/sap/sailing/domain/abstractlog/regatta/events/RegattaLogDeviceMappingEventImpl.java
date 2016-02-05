package com.sap.sailing.domain.abstractlog.regatta.events;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public abstract class RegattaLogDeviceMappingEventImpl<ItemType extends WithID> extends AbstractLogEventImpl<RegattaLogEventVisitor> implements
RegattaLogDeviceMappingEvent<ItemType> {
    private static final long serialVersionUID = -8439653251231710356L;

    private final ItemType mappedTo;
    private final DeviceIdentifier device;
    private final TimePoint from;
    private final TimePoint to;

    public RegattaLogDeviceMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, ItemType mappedTo,
            DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, logicalTimePoint, author, id);
        this.mappedTo = mappedTo;
        this.device = device;
        this.from = from;
        this.to = to;
    }

    public RegattaLogDeviceMappingEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, ItemType mappedTo,
            DeviceIdentifier device, TimePoint from, TimePoint to) {
        this(now(), logicalTimePoint, author, randId(), mappedTo, device, from, to);
    }

    public ItemType getMappedTo() {
        return mappedTo;
    }
    public DeviceIdentifier getDevice() {
        return device;
    }
    public TimePoint getFrom() {
        return from;
    }
    public TimePoint getTo() {
        return to;
    }
    
    @Override
    public String getShortInfo() {
        return "device: " + device + ", mapped to: " + mappedTo + ", from: " + from + ", to: " + to;
    }
}
