package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateCompetitor extends AbstractRacingEventServiceOperation<Competitor> {
    private static final long serialVersionUID = 1172181354320184263L;
    private final Serializable id;
    private final String newName;
    private final String newSailId;
    private final Nationality newNationality;
    
    public UpdateCompetitor(Serializable id, String newName, String newSailId, Nationality newNationality) {
        super();
        this.id = id;
        this.newName = newName;
        this.newSailId = newSailId;
        this.newNationality = newNationality;
    }

    @Override
    public Competitor internalApplyTo(RacingEventService toState) throws Exception {
        return toState.getBaseDomainFactory().getCompetitorStore().updateCompetitor(id, newName, newSailId, newNationality);
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
