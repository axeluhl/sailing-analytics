package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateRegattaLeaderboard extends AbstractRacingEventServiceOperation<RegattaLeaderboard> {
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;
    private final RegattaIdentifier regattaIdentifier;

    public CreateRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds) {
        this.regattaIdentifier = regattaIdentifier;
        this.discardThresholds = discardThresholds;
    }

    @Override
    public RegattaLeaderboard internalApplyTo(RacingEventService toState) {
        RegattaLeaderboard result = toState.addRegattaLeaderboard(regattaIdentifier, discardThresholds);
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
