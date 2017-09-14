package com.sap.sailing.server.anniversary;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.OperationWithResult;

/**
 * Operation for replication of the next anniversary.
 */
public class UpdateNextAnniversaryOperation implements OperationWithResult<RacingEventService, Void> {
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

}
