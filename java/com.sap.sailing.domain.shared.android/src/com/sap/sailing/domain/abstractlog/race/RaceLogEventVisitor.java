package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCScratchBoatEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCSetImpliedWindEvent;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCUseImpliedWindFromOtherRaceEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;

public interface RaceLogEventVisitor {
    void visit(RaceLogRevokeEvent event);
    
    void visit(RaceLogFlagEvent event);

    void visit(RaceLogPassChangeEvent event);

    void visit(RaceLogRaceStatusEvent event);

    void visit(RaceLogStartTimeEvent event);

    void visit(RaceLogCourseDesignChangedEvent event);
    
    void visit(RaceLogFinishPositioningListChangedEvent event);
    
    void visit(RaceLogFinishPositioningConfirmedEvent event);
    
    void visit(RaceLogPathfinderEvent event);
    
    void visit(RaceLogGateLineOpeningTimeEvent event);
    
    void visit(RaceLogStartProcedureChangedEvent event);

    void visit(RaceLogProtestStartTimeEvent event);
    
    void visit(RaceLogWindFixEvent event);
    
    void visit(RaceLogDenoteForTrackingEvent event);
    
    void visit(RaceLogStartTrackingEvent event);

    void visit(RaceLogRegisterCompetitorEvent event);

    void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation);
    
    void visit(RaceLogFixedMarkPassingEvent event);
    
    void visit(RaceLogSuppressedMarkPassingsEvent event);
    
    void visit(RaceLogDependentStartTimeEvent event);
    
    void visit(RaceLogStartOfTrackingEvent event);
    
    void visit(RaceLogEndOfTrackingEvent event);

    void visit(RaceLogUseCompetitorsFromRaceLogEvent event);

    void visit(RaceLogTagEvent event);

    void visit(RaceLogORCLegDataEvent event);
    
    void visit(RaceLogORCCertificateAssignmentEvent event);
    
    void visit(RaceLogORCScratchBoatEvent event);

    void visit(RaceLogORCUseImpliedWindFromOtherRaceEvent event);
    
    void visit(RaceLogORCSetImpliedWindEvent event);
}
