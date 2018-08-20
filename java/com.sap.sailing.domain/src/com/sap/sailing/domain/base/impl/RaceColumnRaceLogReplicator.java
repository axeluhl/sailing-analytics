package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
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
    public void visit(RaceLogDenoteForTrackingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogStartTrackingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogRevokeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogFixedMarkPassingEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation) {
        notifyOnAdd(additionalScoringInformation);
    }

    @Override
    public void visit(RaceLogDependentStartTimeEvent event) {
        notifyOnAdd(event);
    }

    @Override
    public void visit(RaceLogStartOfTrackingEvent event) {
        notifyOnAdd(event);        
    }

    @Override
    public void visit(RaceLogEndOfTrackingEvent event) {
        notifyOnAdd(event);        
    }

    @Override
    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
        notifyOnAdd(event);
    }
}
