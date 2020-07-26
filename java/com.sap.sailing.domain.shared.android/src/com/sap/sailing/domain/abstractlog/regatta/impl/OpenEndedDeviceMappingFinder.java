package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sse.common.WithID;


/**
 * {@link BaseLogAnalyzer} used for finding the ID of an open ended {@link DeviceMappingEvent} to a corresponding
 * {@link RegattaLogCloseOpenEndedDeviceMappingEvent}.<p>
 * 
 * If no corresponding event has been found, {@code null} is returned.
 */
public class OpenEndedDeviceMappingFinder extends BaseLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, Serializable> {
    private final WithID mappedTo;
    private final Serializable deviceUuidAsString;
    
    /**
     * Combines revocations, range closing events and regular mapping events
     */
    private final RegattaLogDeviceMappingFinder<WithID> mappingFinder;
    
    public OpenEndedDeviceMappingFinder(RegattaLog log, WithID mappedTo, String deviceUuidAsString) {
        super(log);
        this.mappingFinder = new RegattaLogDeviceMappingFinder<>(log);
        this.mappedTo = mappedTo;
        this.deviceUuidAsString = deviceUuidAsString;
    }

    @Override
    protected Serializable performAnalysis() {
        final Map<WithID, List<DeviceMappingWithRegattaLogEvent<WithID>>> mappings = mappingFinder.analyze();
        final List<DeviceMappingWithRegattaLogEvent<WithID>> mappingsForObject = mappings.get(mappedTo);
        Serializable result;
        if (mappingsForObject != null) {
            result = null;
            for (final DeviceMappingWithRegattaLogEvent<WithID> mappingEvent : mappingsForObject) {
                if (mappingEvent.getDevice().getStringRepresentation().equals(deviceUuidAsString) &&
                    mappingEvent.getTimeRange().hasOpenEnd()) {
                    result = mappingEvent.getRegattaLogEvent().getId();
                    break;
                }
            }
        } else {
            result = null;
        }
        return result;
    }

}
