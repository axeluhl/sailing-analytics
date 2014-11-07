package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
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
