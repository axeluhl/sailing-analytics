package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateRegattaLeaderboard extends AbstractLeaderboardOperation<RegattaLeaderboard>  {
    private static final Logger logger = Logger.getLogger(CreateRegattaLeaderboard.class.getName());
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;
    private final RegattaIdentifier regattaIdentifier;
    private final String leaderboardDisplayName;

    public CreateRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds) {
        super(((RegattaName) regattaIdentifier).getRegattaName());
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.regattaIdentifier = regattaIdentifier;
        this.discardThresholds = discardThresholds;
    }

    @Override
    public RegattaLeaderboard internalApplyTo(RacingEventService toState) {
        RegattaLeaderboard result = null;
        if (toState.getLeaderboardByName(getLeaderboardName()) == null) {
            result = toState.addRegattaLeaderboard(regattaIdentifier, leaderboardDisplayName, discardThresholds);
        } else {
            logger.warning("Cannot replicate creation of flexible leaderboard "+getLeaderboardName()+" because it already exists in the replica");
        }
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformAddRegattaLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformAddRegattaLeaderboardServerOp(this);
    }
}
