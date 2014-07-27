package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
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
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;

public abstract class AbstractRaceLogChangedVisitor implements RaceLogEventVisitor {
    protected abstract void notifyListenerAboutEventAdded(RaceLogEvent event);

    public void visit(RaceLogFlagEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    public void visit(RaceLogPassChangeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    public void visit(RaceLogRaceStatusEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    public void visit(RaceLogStartTimeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    public void visit(RaceLogCourseAreaChangedEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    public void visit(RaceLogCourseDesignChangedEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        notifyListenerAboutEventAdded(event);        
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(DeviceCompetitorMappingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(DeviceMarkMappingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(DenoteForTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(StartTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RevokeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RegisterCompetitorEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(DefineMarkEvent event) {
        notifyListenerAboutEventAdded(event);
    }
    
    @Override
    public void visit(CloseOpenEndedDeviceMappingEvent event) {
        notifyListenerAboutEventAdded(event);
    }
    
    @Override
    public void visit(AdditionalScoringInformationEvent additionalScoringInformation) {
        notifyListenerAboutEventAdded(additionalScoringInformation);
    }
}
