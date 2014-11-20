package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.racelog.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.FixedMarkPassingEvent;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.SuppressedMarkPassingsEvent;

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
    
    @Override
    public void visit(FixedMarkPassingEvent event){
        
    }

    @Override
    public void visit(SuppressedMarkPassingsEvent event) {
        
    }
}
