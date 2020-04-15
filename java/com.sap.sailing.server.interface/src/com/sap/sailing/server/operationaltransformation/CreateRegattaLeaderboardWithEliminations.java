package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateRegattaLeaderboardWithEliminations extends AbstractLeaderboardOperation<RegattaLeaderboard>  {
    private static final long serialVersionUID = -2851501773630513795L;
    private static final Logger logger = Logger.getLogger(CreateRegattaLeaderboardWithEliminations.class.getName());
    private final String fullRegattaLeaderboardName;
    private final String leaderboardDisplayName;

    public CreateRegattaLeaderboardWithEliminations(String name, String leaderboardDisplayName, String fullRegattaLeaderboardName) {
        super(name);
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.fullRegattaLeaderboardName = fullRegattaLeaderboardName;
    }

    @Override
    public RegattaLeaderboard internalApplyTo(RacingEventService toState) {
        RegattaLeaderboard result = null;
        if (toState.getLeaderboardByName(getLeaderboardName()) == null) {
            result = toState.addRegattaLeaderboardWithEliminations(getLeaderboardName(), leaderboardDisplayName,
                    (RegattaLeaderboard) toState.getLeaderboardByName(fullRegattaLeaderboardName));
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
