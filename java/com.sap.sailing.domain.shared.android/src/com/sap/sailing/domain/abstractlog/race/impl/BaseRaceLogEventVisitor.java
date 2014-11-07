package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.RevokeEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;

/**
 * Base class implementing {@link RaceLogEventVisitor}. Every method is implemented as a no-op.
 */
public abstract class BaseRaceLogEventVisitor implements RaceLogEventVisitor {

    @Override
    public void visit(RaceLogFlagEvent event) {

    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {

    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {

    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {

    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {

    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {

    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {

    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {

    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {

    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {

    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {

    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {

    }

    @Override
    public void visit(RaceLogWindFixEvent event) {

    }
    
    @Override
    public void visit(DeviceCompetitorMappingEvent event) {
    	
    }
    
    @Override
    public void visit(DeviceMarkMappingEvent event) {
    	
    }
    
    @Override
    public void visit(DenoteForTrackingEvent event) {
    	
    }
    
    @Override
    public void visit(StartTrackingEvent event) {
    	
    }
    
    @Override
    public void visit(RevokeEvent event) {
    	
    }
    
    @Override
    public void visit(RegisterCompetitorEvent event) {

    }
    
    @Override
    public void visit(DefineMarkEvent event) {
         
    }

    @Override
    public void visit(CloseOpenEndedDeviceMappingEvent event) {
        
    }
    
    @Override
    public void visit(AdditionalScoringInformationEvent event) {
        
    }
}
