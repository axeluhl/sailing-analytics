package com.sap.sailing.domain.abstractlog.shared.analyzing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.shared.events.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Generates events that close the events that are responsible for the {@link #mapping}
 * that is passed to the constructor.
 * @author Fredrik Teschke
 *
 */
public abstract class OpenEndedDeviceMappingCloser<LogT extends AbstractLog<EventT, VisitorT>,
EventT extends AbstractLogEvent<VisitorT>,VisitorT, CloseEventT extends CloseOpenEndedDeviceMappingEvent<VisitorT>>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, List<CloseEventT>> {
    private final DeviceMapping<?> mapping;
    protected final AbstractLogEventAuthor author;
    protected final TimePoint closingTimePoint;

    public OpenEndedDeviceMappingCloser(LogT log, DeviceMapping<?> mapping, AbstractLogEventAuthor author,
            TimePoint closingTimePoint) {
        super(log);
        this.mapping = mapping;
        this.author = author;
        this.closingTimePoint = closingTimePoint;
    }
    
    protected abstract CloseEventT createCloseEvent(TimePoint logicalTimePoint,
            Serializable eventToCloseId);

    @Override
    protected List<CloseEventT> performAnalysis() {
        List<CloseEventT> result = new ArrayList<CloseEventT>();

        for (Serializable eventId : mapping.getOriginalRaceLogEventIds()) {
            DeviceMappingEvent<?, ?> event = (DeviceMappingEvent<?, ?>) getLog().getEventById(eventId);
            if (event.getFrom() == null || event.getTo() == null) {
                result.add(createCloseEvent(MillisecondsTimePoint.now(), event.getId()));
            }
        }
        return result;
    }
}
