package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMapping;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMappingEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Generates events that close the events that are responsible for the {@link #mapping}
 * that is passed to the constructor.
 * @author Fredrik Teschke
 *
 */
public class OpenEndedDeviceMappingCloser extends RaceLogAnalyzer<List<CloseOpenEndedDeviceMappingEvent>> {
    private final DeviceMapping<?> mapping;
    private final AbstractLogEventAuthor author;
    private final TimePoint closingTimePoint;

    public OpenEndedDeviceMappingCloser(RaceLog raceLog, DeviceMapping<?> mapping, AbstractLogEventAuthor author,
            TimePoint closingTimePoint) {
        super(raceLog);
        this.mapping = mapping;
        this.author = author;
        this.closingTimePoint = closingTimePoint;
    }

    @Override
    protected List<CloseOpenEndedDeviceMappingEvent> performAnalysis() {
        List<CloseOpenEndedDeviceMappingEvent> result = new ArrayList<CloseOpenEndedDeviceMappingEvent>();

        for (Serializable eventId : mapping.getOriginalRaceLogEventIds()) {
            DeviceMappingEvent<?> event = (DeviceMappingEvent<?>) raceLog.getEventById(eventId);
            if (event.getFrom() == null || event.getTo() == null) {
                result.add(RaceLogEventFactory.INSTANCE.createCloseOpenEndedDeviceMappingEvent(
                        MillisecondsTimePoint.now(), author, raceLog.getCurrentPassId(), event.getId(), closingTimePoint));
            }
        }
        return result;
    }
}
