package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sse.common.TimePoint;

public class CloseOpenEndedDeviceMappingEventImpl extends RaceLogEventImpl implements CloseOpenEndedDeviceMappingEvent {
    private static final long serialVersionUID = -2401732623610224918L;
    
    private final Serializable deviceMappingEventId;
    private final TimePoint closingTimePoint;

    public CloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int pPassId,
            Serializable deviceMappingEventId, TimePoint closingTimePoint) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
        this.deviceMappingEventId = deviceMappingEventId;
        this.closingTimePoint = closingTimePoint;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Serializable getDeviceMappingEventId() {
        return deviceMappingEventId;
    }

    @Override
    public TimePoint getClosingTimePoint() {
        return closingTimePoint;
    }

}
