package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.AbstractDeviceMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class RegattaLogDeviceCompetitorMappingEventImpl extends
        AbstractDeviceMappingEventImpl<RegattaLogEventVisitor, Competitor> implements
        RegattaLogDeviceCompetitorMappingEvent {
    public RegattaLogDeviceCompetitorMappingEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, pId, mappedTo, device, from, to);
    }

    public RegattaLogDeviceCompetitorMappingEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        super(author, logicalTimePoint, mappedTo, device, from, to);
    }

    private static final long serialVersionUID = -1494030544804758753L;

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
