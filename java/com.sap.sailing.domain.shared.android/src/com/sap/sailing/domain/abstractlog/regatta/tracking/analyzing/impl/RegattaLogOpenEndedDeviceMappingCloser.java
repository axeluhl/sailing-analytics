package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Generates events that close the events that are responsible for the {@link #mapping} that is passed to the
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class RegattaLogOpenEndedDeviceMappingCloser extends
        RegattaLogAnalyzer<List<RegattaLogCloseOpenEndedDeviceMappingEvent>> {
    private final DeviceMapping<?> mapping;
    protected final AbstractLogEventAuthor author;
    protected final TimePoint closingTimePointInclusive;

    public RegattaLogOpenEndedDeviceMappingCloser(RegattaLog log, DeviceMapping<?> mapping,
            AbstractLogEventAuthor author, TimePoint closingTimePointInclusive) {
        super(log);
        this.mapping = mapping;
        this.author = author;
        this.closingTimePointInclusive = closingTimePointInclusive;
    }

    protected RegattaLogCloseOpenEndedDeviceMappingEvent createCloseEvent(TimePoint logicalTimePoint,
            Serializable eventToCloseId) {
        return new RegattaLogCloseOpenEndedDeviceMappingEventImpl(logicalTimePoint, author, eventToCloseId,
                closingTimePointInclusive);
    }

    @Override
    protected List<RegattaLogCloseOpenEndedDeviceMappingEvent> performAnalysis() {
        List<RegattaLogCloseOpenEndedDeviceMappingEvent> result = new ArrayList<RegattaLogCloseOpenEndedDeviceMappingEvent>();

        for (Serializable eventId : mapping.getOriginalRaceLogEventIds()) {
            RegattaLogDeviceMappingEvent<?> event = (RegattaLogDeviceMappingEvent<?>) getLog().getEventById(eventId);
            if (event.getFrom() == null || event.getToInclusive() == null) {
                result.add(createCloseEvent(MillisecondsTimePoint.now(), event.getId()));
            }
        }
        return result;
    }
}
