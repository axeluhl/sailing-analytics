package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class DeviceCompetitorMappingEventImpl extends AbstractDeviceMappingEventImpl<Competitor>
implements DeviceCompetitorMappingEvent {
    public DeviceCompetitorMappingEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Competitor mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, pId, pPassId, mappedTo, device, from, to);
    }

    private static final long serialVersionUID = -1494030544804758753L;

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
