package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithOtherTieBreakingLeaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateRegattaLeaderboardWithOtherTieBreakingLeaderboard extends AbstractLeaderboardOperation<RegattaLeaderboardWithOtherTieBreakingLeaderboard> {
    private static final long serialVersionUID = -2851501773630513795L;
    private static final Logger logger = Logger.getLogger(CreateRegattaLeaderboardWithOtherTieBreakingLeaderboard.class.getName());
    private final RegattaIdentifier regattaIdentifier;
    private final String leaderboardDisplayName;
    private final int[] discardThresholds;
    private final String otherTieBreakingLeaderboardName;

    public CreateRegattaLeaderboardWithOtherTieBreakingLeaderboard(RegattaIdentifier regattaIdentifier,
            String leaderboardDisplayName, int[] discardThresholds, String otherTieBreakingLeaderboardName) {
        super(((RegattaName) regattaIdentifier).getRegattaName());
        this.regattaIdentifier = regattaIdentifier;
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.discardThresholds = discardThresholds;
        this.otherTieBreakingLeaderboardName = otherTieBreakingLeaderboardName;
    }

    @Override
    public RegattaLeaderboardWithOtherTieBreakingLeaderboard internalApplyTo(RacingEventService toState) {
        RegattaLeaderboardWithOtherTieBreakingLeaderboard result = null;
        if (toState.getLeaderboardByName(getLeaderboardName()) == null) {
            result = toState.addRegattaLeaderboardWithOtherTieBreakingLeaderboard(regattaIdentifier, leaderboardDisplayName,
                    discardThresholds, (RegattaLeaderboard) toState.getLeaderboardByName(otherTieBreakingLeaderboardName));
        } else {
            logger.warning("Cannot replicate creation of regatta leaderboard "+getLeaderboardName()+" because it already exists in the replica");
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
