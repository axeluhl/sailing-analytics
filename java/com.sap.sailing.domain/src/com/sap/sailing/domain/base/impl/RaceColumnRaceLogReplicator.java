package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.FixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.SuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public class RaceColumnRaceLogReplicator implements RaceLogEventVisitor, Serializable {
    private static final long serialVersionUID = 4752330236765192592L;
    
    private final RaceColumn raceColumn;
    private final RaceLogIdentifier identifier;
    
    public RaceColumnRaceLogReplicator(RaceColumn raceColumn, RaceLogIdentifier identifier) {
        this.raceColumn = raceColumn;
        this.identifier = identifier;
    }
        
    private void notifyOnAdd(RaceLogEvent event) {
        raceColumn.getRaceColumnListeners().notifyListenersAboutRaceLogEventAdded(
                raceColumn, 
                identifier, 
                event);
    }
    
    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogStartTimeEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogPassChangeEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(RaceLogFlagEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(DeviceCompetitorMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(DeviceMarkMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(DenoteForTrackingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(StartTrackingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogRevokeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RegisterCompetitorEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(DefineMarkEvent event) {
        notifyOnAdd(event);
    }
    
    @Override
    public void visit(CloseOpenEndedDeviceMappingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(FixedMarkPassingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(SuppressedMarkPassingsEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(AdditionalScoringInformationEvent additionalScoringInformation) {
        notifyOnAdd(additionalScoringInformation);
    }
}
