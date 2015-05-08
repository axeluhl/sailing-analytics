package com.sap.sailing.domain.abstractlog.regatta.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMile;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactor;

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
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactor event) {
        eventAdded(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMile event) {
        eventAdded(event);
    }
}
