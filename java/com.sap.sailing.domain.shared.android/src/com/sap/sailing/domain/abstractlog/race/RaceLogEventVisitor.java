package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.CloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;


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
    
    public void visit(DeviceCompetitorMappingEvent event);
    
    public void visit(DeviceMarkMappingEvent event);
    
    public void visit(DenoteForTrackingEvent event);
    
    public void visit(StartTrackingEvent event);
    
    public void visit(RegisterCompetitorEvent event);
    
    public void visit(DefineMarkEvent event);
    
    public void visit(CloseOpenEndedDeviceMappingEvent event);

    public void visit(FixedMarkPassingEvent event);
    
    public void visit(AdditionalScoringInformationEvent additionalScoringInformation);

    public void visit(SuppressedMarkPassingsEvent event);
}
