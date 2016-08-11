package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sse.common.WithID;


/**
 * BaseLogAnalyzer used for finding the ID of an open ended @link{DeviceMappingEvent} to a corresponding @link{CloseOpenEndedDeviceMappingEvent}
 * 
 * If no corresponding event has been found, null is returned
 */
public class OpenEndedDeviceMappingFinder extends BaseLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor, Serializable> {

    private final WithID mappedTo;
    private final Serializable deviceUuid;
    
    public OpenEndedDeviceMappingFinder(RegattaLog log, WithID mappedTo, Serializable deviceUuid) {
        super(log);
        this.mappedTo = mappedTo;
        this.deviceUuid = deviceUuid;
    }

    @Override
    protected Serializable performAnalysis() {
        for (RegattaLogEvent regattaLogEvent : log.getUnrevokedEvents()) {
            if (regattaLogEvent instanceof RegattaLogDeviceMappingEvent<?>) {
                RegattaLogDeviceMappingEvent<?> deviceMappingEvent = (RegattaLogDeviceMappingEvent<?>) regattaLogEvent;
                if (deviceMappingEvent.getMappedTo().equals(mappedTo) &&
                        deviceMappingEvent.getDevice().getStringRepresentation().equals(deviceUuid)) {
                    return deviceMappingEvent.getId();
                }
            }
        }
        return null;
    }

}
