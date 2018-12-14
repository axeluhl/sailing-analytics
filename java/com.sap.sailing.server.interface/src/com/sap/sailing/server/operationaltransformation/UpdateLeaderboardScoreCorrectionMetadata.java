package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

public class UpdateLeaderboardScoreCorrectionMetadata extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = -977025759476022993L;
    private final TimePoint timePointOfLastCorrectionValidity;
    private final String comment;
    
    /**
     * @param timePoint the time point for which to deliver leaderboard results as the result of this operation
     */
    public UpdateLeaderboardScoreCorrectionMetadata(String leaderboardName, TimePoint timePointOfLastCorrectionValidity, String comment) {
        super(leaderboardName);
        this.timePointOfLastCorrectionValidity = timePointOfLastCorrectionValidity;
        this.comment = comment;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws NoWindException {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
            if (scoreCorrection != null) {
                scoreCorrection.setComment(comment);
                scoreCorrection.setTimePointOfLastCorrectionsValidity(timePointOfLastCorrectionValidity);
            } else {
                throw new IllegalArgumentException("Leaderboard "+getLeaderboardName()+" has no score corrections to which to apply metadata "+
                        timePointOfLastCorrectionValidity+"/"+comment);
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        updateStoredLeaderboard(toState, leaderboard);
        return null;
    }

}
