package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.shared.analyzing.OpenEndedDeviceMappingCloser;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaLogOpenEndedDeviceMappingCloser extends
        OpenEndedDeviceMappingCloser<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, RegattaLogCloseOpenEndedDeviceMappingEvent> {

    public RegattaLogOpenEndedDeviceMappingCloser(RegattaLog log, DeviceMapping<?> mapping, AbstractLogEventAuthor author,
            TimePoint closingTimePoint) {
        super(log, mapping, author, closingTimePoint);
    }

    @Override
    protected RegattaLogCloseOpenEndedDeviceMappingEvent createCloseEvent(TimePoint logicalTimePoint,
            Serializable eventToCloseId) {
        return new RegattaLogCloseOpenEndedDeviceMappingEventImpl(MillisecondsTimePoint.now(), author,
                logicalTimePoint, UUID.randomUUID(), eventToCloseId, closingTimePoint);
    }
}
