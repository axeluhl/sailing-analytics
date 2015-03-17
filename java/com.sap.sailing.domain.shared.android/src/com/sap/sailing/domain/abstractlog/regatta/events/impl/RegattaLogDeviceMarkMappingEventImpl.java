package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.AbstractDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogDeviceMarkMappingEventImpl extends AbstractDeviceMappingEventImpl<RegattaLogEventVisitor, Mark>
implements RegattaLogDeviceMarkMappingEvent {
    private static final long serialVersionUID = -7223543830755457196L;

    public RegattaLogDeviceMarkMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Mark mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, pId, mappedTo, device, from, to);
    }

    public RegattaLogDeviceMarkMappingEventImpl(AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Mark mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        this(MillisecondsTimePoint.now(), author, logicalTimePoint, UUID.randomUUID(), mappedTo, device, from, to);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
