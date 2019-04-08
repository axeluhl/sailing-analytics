package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.Color;

public class CreateBoat extends AbstractRacingEventServiceOperation<Boat> {
    private static final long serialVersionUID = 4868428194661255331L;
    private final Serializable boatId;
    private final String name;
    private final String boatClassName;
    private final Color color;
    private final String sailId;
    
    public CreateBoat(Serializable boatId, String name, String boatClassName, String sailId, Color color) {
        super();
        this.boatId = boatId;
        this.name = name;
        this.boatClassName = boatClassName;
        this.sailId = sailId;
        this.color = color;
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
        BoatClass boatClass = toState.getBaseDomainFactory().getOrCreateBoatClass(boatClassName);
        Boat result = toState.getBaseDomainFactory().getCompetitorAndBoatStore().getOrCreateBoat(boatId, name, boatClass, sailId, color);
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
