package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;


public interface RaceLogEventVisitor {
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
    
    public void visit(DeviceCompetitorMappingEvent event);
    
    public void visit(DeviceMarkMappingEvent event);
    
    public void visit(DenoteForTrackingEvent event);
    
    public void visit(StartTrackingEvent event);
    
    public void visit(RevokeEvent event);
    
    public void visit(RegisterCompetitorEvent event);
}
