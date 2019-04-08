package com.sap.sailing.domain.abstractlog.race.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
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
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;

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
    public void visit(RaceLogDenoteForTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogStartTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogRevokeEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {
        notifyListenerAboutEventAdded(event);
    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent additionalScoringInformation) {
        notifyListenerAboutEventAdded(additionalScoringInformation);
    }
    
    @Override
    public void visit(RaceLogFixedMarkPassingEvent fixedMarkPassingEvent) {
        notifyListenerAboutEventAdded(fixedMarkPassingEvent);
    }
    
    @Override
    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
        notifyListenerAboutEventAdded(event);        
    }
    
    @Override
    public void visit(RaceLogDependentStartTimeEvent event) {
        notifyListenerAboutEventAdded(event);        
    }
    
    @Override
    public void visit(RaceLogStartOfTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }
    
    @Override
    public void visit(RaceLogEndOfTrackingEvent event) {
        notifyListenerAboutEventAdded(event);
    }
    
    @Override
    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
        notifyListenerAboutEventAdded(event);
    }
    
    @Override
    public void visit(RaceLogTagEvent event) {
        notifyListenerAboutEventAdded(event);
    }
}
