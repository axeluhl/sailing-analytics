package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.base.RaceColumn;

public class RaceColumnRegattaLogReplicator implements RegattaLogEventVisitor, Serializable {
    private static final long serialVersionUID = 7042478407494947820L;
    private final RaceColumn raceColumn;
    
    public RaceColumnRegattaLogReplicator(RaceColumn raceColumn) {
        super();
        this.raceColumn = raceColumn;
    }
    
    private void notifyOnAdd(RegattaLogEvent event) {
        raceColumn.getRaceColumnListeners().notifyListenersAboutRegattaLogEventAdded(raceColumn, event);
    }

    @Override
    public void visit(RegattaLogRevokeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogDeviceMarkMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegattaLogDefineMarkEvent event) {
        notifyOnAdd(event);
    }
}
