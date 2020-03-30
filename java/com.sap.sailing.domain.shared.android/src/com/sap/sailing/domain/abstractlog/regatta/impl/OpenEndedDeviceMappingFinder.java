package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
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
    
    public OpenEndedDeviceMappingFinder(RegattaLog log, WithID mappedTo, String deviceUuidAsString) {
        super(log);
        this.mappedTo = mappedTo;
        this.deviceUuidAsString = deviceUuidAsString;
    }

    @Override
    protected Serializable performAnalysis() {
        for (RegattaLogEvent regattaLogEvent : log.getUnrevokedEvents()) {
            if (regattaLogEvent instanceof RegattaLogDeviceMappingEvent<?>) {
                RegattaLogDeviceMappingEvent<?> deviceMappingEvent = (RegattaLogDeviceMappingEvent<?>) regattaLogEvent;
                if (deviceMappingEvent.getMappedTo().equals(mappedTo) &&
                        deviceMappingEvent.getDevice().getStringRepresentation().equals(deviceUuidAsString)) {
                    return deviceMappingEvent.getId();
                }
            }
        }
        return null;
    }

}
