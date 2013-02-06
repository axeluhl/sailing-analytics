package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateRegattaLeaderboard extends AbstractRacingEventServiceOperation<RegattaLeaderboard> {
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;
    private final RegattaIdentifier regattaIdentifier;
    private final Serializable courseAreaId;

    public CreateRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds, Serializable courseAreaId) {
        this.regattaIdentifier = regattaIdentifier;
        this.discardThresholds = discardThresholds;
        this.courseAreaId = courseAreaId;
    }

    @Override
    public RegattaLeaderboard internalApplyTo(RacingEventService toState) {
        RegattaLeaderboard result = toState.addRegattaLeaderboard(regattaIdentifier, discardThresholds, courseAreaId);
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO
        return null;
    }

}
