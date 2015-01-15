package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;


public interface RaceLogEventVisitor {
    public void visit(RaceLogRevokeEvent event);
    
    public void visit(RaceLogFlagEvent event);

    public void visit(RaceLogPassChangeEvent event);

    public void visit(RaceLogRaceStatusEvent event);

    public void visit(RaceLogStartTimeEvent event);

    public void visit(RaceLogCourseAreaChangedEvent event);
    
    public void visit(RaceLogCourseDesignChangedEvent event);
    
    public void visit(RaceLogFinishPositioningListChangedEvent event);
    
    public void visit(RaceLogFinishPositioningConfirmedEvent event);
    
    public void visit(RaceLogPathfinderEvent event);
    
    public void visit(RaceLogGateLineOpeningTimeEvent event);
    
    public void visit(RaceLogStartProcedureChangedEvent event);

    public void visit(RaceLogProtestStartTimeEvent event);
    
    public void visit(RaceLogWindFixEvent event);
    
    public void visit(RaceLogDeviceCompetitorMappingEvent event);
    
    public void visit(RaceLogDeviceMarkMappingEvent event);
    
    public void visit(RaceLogDenoteForTrackingEvent event);
    
    public void visit(RaceLogStartTrackingEvent event);
    
    public void visit(RaceLogRegisterCompetitorEvent event);
    
    public void visit(RaceLogDefineMarkEvent event);
    
    public void visit(RaceLogCloseOpenEndedDeviceMappingEvent event);

    public void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation);
}
