package com.sap.sailing.domain.abstractlog.race.tracking.events;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class DeviceCompetitorMappingEventImpl extends AbstractDeviceMappingEventImpl<Competitor>
implements DeviceCompetitorMappingEvent {
    public DeviceCompetitorMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Competitor mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, pId, pPassId, mappedTo, device, from, to);
    }

    private static final long serialVersionUID = -1494030544804758753L;

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
