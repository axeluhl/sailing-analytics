package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sse.common.TimePoint;

public class RegattaLogCloseOpenEndedDeviceMappingEventImpl extends BaseCloseOpenEndedDeviceMappingEventImpl<RegattaLogEventVisitor>
implements RegattaLogCloseOpenEndedDeviceMappingEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    
    public RegattaLogCloseOpenEndedDeviceMappingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Serializable deviceMappingEventId,
            TimePoint closingTimePoint) {
        super(createdAt, author, logicalTimePoint, id, deviceMappingEventId, closingTimePoint);
    }
    
    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
