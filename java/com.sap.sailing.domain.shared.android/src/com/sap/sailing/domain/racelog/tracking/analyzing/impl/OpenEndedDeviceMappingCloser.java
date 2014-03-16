package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMapping;
import com.sap.sailing.domain.racelog.tracking.DeviceMappingEvent;

/**
 * Generates an event that closes the event that most likely is responsible for the {@link #mapping}
 * that is passed to the constructor.
 * 
 * Currently, the implementation is rather naive and just looks for the last event for that device and
 * item, that has an open end.
 * @author Fredrik Teschke
 *
 */
public class OpenEndedDeviceMappingCloser extends RaceLogAnalyzer<CloseOpenEndedDeviceMappingEvent> {
    private final DeviceMapping<?> mapping;
    private final RaceLogEventAuthor author;
    private final TimePoint closingTimePoint;
    
    public OpenEndedDeviceMappingCloser(RaceLog raceLog, DeviceMapping<?> mapping, RaceLogEventAuthor author,
            TimePoint closingTimePoint) {
        super(raceLog);
        this.mapping = mapping;
        this.author = author;
        this.closingTimePoint = closingTimePoint;
    }

    @Override
    protected CloseOpenEndedDeviceMappingEvent performAnalysis() {
        for (RaceLogEvent e : raceLog.getUnrevokedEventsDescending()) {
            if (e instanceof DeviceMappingEvent) {
                @SuppressWarnings("unchecked")
                DeviceMappingEvent<? extends WithID> event = (DeviceMappingEvent<? extends WithID>) e;
                if (mapping.getDevice().equals(event.getDevice()) && mapping.getMappedTo().equals(event.getMappedTo())
                        && (event.getTo() == null || event.getFrom() == null)) {
                    return RaceLogEventFactory.INSTANCE.createCloseOpenEndedDeviceMappingEvent(
                            MillisecondsTimePoint.now(), author, raceLog.getCurrentPassId(), event.getId(), closingTimePoint);
                }
            }
        }
        return null;
    }

}
