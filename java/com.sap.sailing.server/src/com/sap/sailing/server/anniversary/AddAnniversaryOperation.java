package com.sap.sailing.server.anniversary;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.OperationWithResult;

/**
 * Operation for replication of an newly added anniversary race.
 */
public class AddAnniversaryOperation implements OperationWithResult<RacingEventService, Void> {
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

}
