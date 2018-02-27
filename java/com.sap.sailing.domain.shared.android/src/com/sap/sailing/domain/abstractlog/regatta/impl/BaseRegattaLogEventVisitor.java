package com.sap.sailing.domain.abstractlog.regatta.impl;

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

public class BaseRegattaLogEventVisitor implements RegattaLogEventVisitor {

    @Override
    public void visit(RegattaLogRevokeEvent event) {
    }

    @Override
    public void visit(RegattaLogDeviceMarkMappingEvent event) {
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
    }
    
    @Override
    public void visit(RegattaLogDeviceBoatMappingEvent event) {
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
    }
    
    @Override
    public void visit(RegattaLogDeviceBoatSensorDataMappingEvent event) {
    }

    @Override
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
    }

    @Override
    public void visit(RegattaLogRegisterBoatEvent event) {
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
    }

    @Override
    public void visit(RegattaLogDefineMarkEvent event) {
    }
}
