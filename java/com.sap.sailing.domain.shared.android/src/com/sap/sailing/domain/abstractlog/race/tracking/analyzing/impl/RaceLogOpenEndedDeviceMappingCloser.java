package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.analyzing.OpenEndedDeviceMappingCloser;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;

public class RaceLogOpenEndedDeviceMappingCloser extends
        OpenEndedDeviceMappingCloser<RaceLog, RaceLogEvent, RaceLogEventVisitor, RaceLogCloseOpenEndedDeviceMappingEvent> {

    public RaceLogOpenEndedDeviceMappingCloser(RaceLog log, DeviceMapping<?> mapping, AbstractLogEventAuthor author,
            TimePoint closingTimePoint) {
        super(log, mapping, author, closingTimePoint);
    }

    @Override
    protected RaceLogCloseOpenEndedDeviceMappingEvent createCloseEvent(TimePoint logicalTimePoint,
            Serializable eventToCloseId) {
        return RaceLogEventFactory.INSTANCE.createCloseOpenEndedDeviceMappingEvent(
                logicalTimePoint, author, getLog().getCurrentPassId(), eventToCloseId, closingTimePoint);
    }

}
