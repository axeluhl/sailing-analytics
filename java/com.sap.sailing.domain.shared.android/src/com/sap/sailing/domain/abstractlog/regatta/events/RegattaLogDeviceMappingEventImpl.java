package com.sap.sailing.domain.abstractlog.regatta.events;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public abstract class RegattaLogDeviceMappingEventImpl<ItemType extends WithID> extends AbstractLogEventImpl<RegattaLogEventVisitor> implements
RegattaLogDeviceMappingEvent<ItemType> {
    private static final long serialVersionUID = -8439653251231710356L;

    public interface Factory<ItemType extends WithID, T extends RegattaLogDeviceMappingEvent<ItemType>> {
        T create(TimePoint createdAt, TimePoint logicalTimePoint,
                AbstractLogEventAuthor author, Serializable pId, ItemType mappedTo, DeviceIdentifier device,
                TimePoint from, TimePoint to);
    }

    private final ItemType mappedTo;
    private final DeviceIdentifier device;
    
    /**
     * inclusive start time point
     */
    private final TimePoint from;
    
    /**
     * inclusive end time point
     */
    private final TimePoint toInclusive;

    /**
     * @param from inclusive
     * @param toInclusive inclusive
     */
    public RegattaLogDeviceMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, ItemType mappedTo,
            DeviceIdentifier device, TimePoint from, TimePoint toInclusive) {
        super(createdAt, logicalTimePoint, author, id);
        this.mappedTo = mappedTo;
        this.device = device;
        this.from = from;
        this.toInclusive = toInclusive;
    }

    public RegattaLogDeviceMappingEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, ItemType mappedTo,
            DeviceIdentifier device, TimePoint from, TimePoint toInclusive) {
        this(now(), logicalTimePoint, author, randId(), mappedTo, device, from, toInclusive);
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
    public TimePoint getToInclusive() {
        return toInclusive;
    }
    
    @Override
    public String getShortInfo() {
        return "device: " + device + ", mapped to: " + mappedTo + ", from: " + from + ", to: " + toInclusive;
    }
}
