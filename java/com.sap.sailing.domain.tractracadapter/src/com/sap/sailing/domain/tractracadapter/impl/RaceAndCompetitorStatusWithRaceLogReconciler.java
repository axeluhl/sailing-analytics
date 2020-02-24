package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;

/**
 * A service that understands the different {@link IRaceCompetitor#getStatus() competitor statuses} and the
 * {@link IRace#getStatus() race status} and can reconcile them with the {@link RaceLog} of a {@link TrackedRace} such
 * that afterwards the {@link RaceLog} is guaranteed to describe the competitor status accordingly. When the
 * reconciliation is requested and the {@link RaceLog} already represents the competitor status appropriately, no
 * changes will be applied to the race log.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceAndCompetitorStatusWithRaceLogReconciler {
    private final DomainFactory domainFactory;
    
    public RaceAndCompetitorStatusWithRaceLogReconciler(DomainFactory domainFactory) {
        super();
        this.domainFactory = domainFactory;
    }

    public void reconcileRaceStatus(IRace tractracRace, TrackedRace trackedRace) {
        
    }

    public void reconcileCompetitorStatus(IRaceCompetitor tracTracCompetitor, TrackedRace trackedRace) {
        
    }
}
