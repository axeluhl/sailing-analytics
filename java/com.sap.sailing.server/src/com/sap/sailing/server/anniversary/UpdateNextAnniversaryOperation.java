package com.sap.sailing.server.anniversary;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.OperationWithResult;

public class UpdateNextAnniversaryOperation implements OperationWithResult<RacingEventService, Void> {
    private static final long serialVersionUID = 8989228640752085561L;
    private final Pair<Integer, AnniversaryType> nextAnniversaryNumber;
    
    public UpdateNextAnniversaryOperation(Pair<Integer, AnniversaryType> nextAnniversaryNumber) {
        this.nextAnniversaryNumber = nextAnniversaryNumber;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.getAnniversaryRaceDeterminator().setNextAnniversary(nextAnniversaryNumber);
        return null;
    }

}
