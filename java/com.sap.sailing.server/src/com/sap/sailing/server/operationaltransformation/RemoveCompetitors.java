package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveCompetitors extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 4620938724207264193L;
    private final Iterable<Competitor> competitorsToRemove;
    
    public RemoveCompetitors(Iterable<Competitor> competitorsToRemove) {
        super();
        this.competitorsToRemove = competitorsToRemove;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        for (Competitor competitor : competitorsToRemove) {
            toState.getBaseDomainFactory().getCompetitorStore().removeCompetitor(competitor);
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
