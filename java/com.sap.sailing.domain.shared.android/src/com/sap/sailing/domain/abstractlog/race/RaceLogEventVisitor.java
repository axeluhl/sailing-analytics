package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;


public interface RaceLogEventVisitor {
    public void visit(RaceLogRevokeEvent event);
    
    public void visit(RaceLogFlagEvent event);

    public void visit(RaceLogPassChangeEvent event);

    public void visit(RaceLogRaceStatusEvent event);

    public void visit(RaceLogStartTimeEvent event);

    public void visit(RaceLogCourseDesignChangedEvent event);
    
    public void visit(RaceLogFinishPositioningListChangedEvent event);
    
    public void visit(RaceLogFinishPositioningConfirmedEvent event);
    
    public void visit(RaceLogPathfinderEvent event);
    
    public void visit(RaceLogGateLineOpeningTimeEvent event);
    
    public void visit(RaceLogStartProcedureChangedEvent event);

    public void visit(RaceLogProtestStartTimeEvent event);
    
    public void visit(RaceLogWindFixEvent event);
    
    public void visit(RaceLogDenoteForTrackingEvent event);
    
    public void visit(RaceLogStartTrackingEvent event);

    public void visit(RaceLogRegisterCompetitorEvent event);

    public void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation);
    
    public void visit(RaceLogFixedMarkPassingEvent event);
    
    public void visit(RaceLogSuppressedMarkPassingsEvent event);
    
    public void visit(RaceLogDependentStartTimeEvent event);
    
    public void visit(RaceLogStartOfTrackingEvent event);
    
    public void visit(RaceLogEndOfTrackingEvent event);

    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event);
}
