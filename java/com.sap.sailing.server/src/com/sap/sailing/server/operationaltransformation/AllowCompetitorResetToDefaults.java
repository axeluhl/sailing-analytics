package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AllowCompetitorResetToDefaults extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 5133140671156755328L;
    private final String competitorIdAsString;
    
    public AllowCompetitorResetToDefaults(String competitorIdAsString) {
        super();
        this.competitorIdAsString = competitorIdAsString;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        final CompetitorStore competitorStore = toState.getBaseDomainFactory().getCompetitorStore();
        Competitor competitor = competitorStore.getExistingCompetitorByIdAsString(competitorIdAsString);
        if (competitor != null) {
            competitorStore.allowCompetitorResetToDefaults(competitor);
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
