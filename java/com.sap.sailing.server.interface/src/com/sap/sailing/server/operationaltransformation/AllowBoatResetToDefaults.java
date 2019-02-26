package com.sap.sailing.server.operationaltransformation;

import java.util.ArrayList;

import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.Util;

public class AllowBoatResetToDefaults extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -3698113910122095903L;
    private final Iterable<String> boatIdsAsStrings;
    
    public AllowBoatResetToDefaults(Iterable<String> boatIdsAsStrings) {
        super();
        final ArrayList<String> arrayList = new ArrayList<>(); // to guarantee serializability, even if an unmodifiable or singleton is passed
        this.boatIdsAsStrings = arrayList;
        Util.addAll(boatIdsAsStrings, arrayList);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        final CompetitorAndBoatStore competitorAndBoatStore = toState.getBaseDomainFactory().getCompetitorAndBoatStore();
        for (String boatIdAsString : boatIdsAsStrings) {
            DynamicBoat boat = competitorAndBoatStore.getExistingBoatByIdAsString(boatIdAsString);
            if (boat != null) {
                competitorAndBoatStore.allowBoatResetToDefaults(boat);
            }
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
