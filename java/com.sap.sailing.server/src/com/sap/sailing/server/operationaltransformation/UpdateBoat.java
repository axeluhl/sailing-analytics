package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.Color;

public class UpdateBoat extends AbstractRacingEventServiceOperation<Boat> {
    private static final long serialVersionUID = 1341635844670894476L;
    private final String idAsString;
    private final String newName;
    private final Color newColor;
    private final String newSailId;
    
    /**
     * @param idAsString
     *            Identifies the boat to update
     */
    public UpdateBoat(String idAsString, String newName, Color newColor, String newSailId) {
        super();
        this.idAsString = idAsString;
        this.newName = newName;
        this.newColor = newColor;
        this.newSailId = newSailId;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public Boat internalApplyTo(RacingEventService toState) throws Exception {
        Boat result = toState.getBaseDomainFactory().getCompetitorAndBoatStore().updateBoat(idAsString, newName, newColor, newSailId);
        return result;
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
