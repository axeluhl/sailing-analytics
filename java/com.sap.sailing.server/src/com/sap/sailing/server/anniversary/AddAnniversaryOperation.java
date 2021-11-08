package com.sap.sailing.server.anniversary;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sailing.server.operationaltransformation.AbstractRacingEventServiceOperation;
import com.sap.sse.common.Util.Pair;

/**
 * Operation for replication of an newly added anniversary race.
 */
public class AddAnniversaryOperation extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 8989228640752085561L;
    
    private final int anniversaryToCheck;
    private final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData;

    public AddAnniversaryOperation(int anniversaryToCheck, final Pair<DetailedRaceInfo, AnniversaryType> anniversaryData) {
        this.anniversaryToCheck = anniversaryToCheck;
        this.anniversaryData = anniversaryData;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.getAnniversaryRaceDeterminator().addAnniversary(anniversaryToCheck, anniversaryData);
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
