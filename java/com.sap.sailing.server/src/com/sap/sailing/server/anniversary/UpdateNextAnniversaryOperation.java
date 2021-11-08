package com.sap.sailing.server.anniversary;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.AbstractRacingEventServiceOperation;
import com.sap.sse.common.Util.Pair;

/**
 * Operation for replication of the next anniversary.
 */
public class UpdateNextAnniversaryOperation extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 8989228640752085561L;
    private final Pair<Integer, AnniversaryType> nextAnniversary;
    
    public UpdateNextAnniversaryOperation(Pair<Integer, AnniversaryType> nextAnniversary) {
        this.nextAnniversary = nextAnniversary;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.getAnniversaryRaceDeterminator().setNextAnniversary(nextAnniversary);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }

}
