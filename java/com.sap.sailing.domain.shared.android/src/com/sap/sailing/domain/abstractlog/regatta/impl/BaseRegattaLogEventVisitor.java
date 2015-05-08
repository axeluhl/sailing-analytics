package com.sap.sailing.domain.abstractlog.regatta.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMile;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactor;

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
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactor event) {
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMile event) {
    }

}
