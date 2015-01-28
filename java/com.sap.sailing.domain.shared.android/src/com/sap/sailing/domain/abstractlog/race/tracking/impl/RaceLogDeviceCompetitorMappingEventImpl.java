package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

public class RaceLogDeviceCompetitorMappingEventImpl extends AbstractRaceLogDeviceMappingEventImpl<Competitor>
implements RaceLogDeviceCompetitorMappingEvent {
    public RaceLogDeviceCompetitorMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId, Competitor mappedTo, DeviceIdentifier device, TimePoint from, TimePoint to) {
        super(createdAt, author, logicalTimePoint, pId, pPassId, mappedTo, device, from, to);
    }

    private static final long serialVersionUID = -1494030544804758753L;

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
