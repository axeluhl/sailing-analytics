package com.sap.sailing.domain.abstractlog.regatta.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;

public abstract class RegattaLogEventListener implements RegattaLogEventVisitor {
    protected abstract void eventAdded(RegattaLogEvent event);

    @Override
    public void visit(RegattaLogRevokeEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogDeviceMarkMappingEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
        eventAdded(event);
    }
    
    @Override
    public void visit(RegattaLogDeviceBoatMappingEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogRegisterBoatEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogDefineMarkEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
        eventAdded(event);
    }
    
    @Override
    public void visit(RegattaLogDeviceBoatSensorDataMappingEvent event) {
        eventAdded(event);
    }
}
