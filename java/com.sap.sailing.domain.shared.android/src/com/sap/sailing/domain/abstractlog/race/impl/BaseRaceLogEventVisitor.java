package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
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
    public void visit(RaceLogDenoteForTrackingEvent event) {
    	
    }
    
    @Override
    public void visit(RaceLogStartTrackingEvent event) {
    	
    }
    
    @Override
    public void visit(RaceLogRevokeEvent event) {
    	
    }
    
    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {

    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent event) {
        
    }
    
    @Override
    public void visit(RaceLogFixedMarkPassingEvent event){
        
    }

    @Override
    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
        
    }
    
    @Override
    public void visit(RaceLogDependentStartTimeEvent event) {
        
    }
    
    @Override
    public void visit(RaceLogEndOfTrackingEvent event) {
        
    }
    
    @Override
    public void visit(RaceLogStartOfTrackingEvent event) {
        
    }
    
    @Override
    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
        
    }
    
}
