package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard.Entry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class UpdateLeaderboardMaxPointsReason extends AbstractLeaderboardColumnOperation<Util.Triple<Double, Double, Boolean>> {
    private static final long serialVersionUID = -492130952256848047L;
    private final String competitorIdAsString;
    private final MaxPointsReason newMaxPointsReason;
    private final TimePoint timePoint;
    
    public UpdateLeaderboardMaxPointsReason(String leaderboardName, String columnName, String competitorIdAsString,
            MaxPointsReason newMaxPointsReason, TimePoint timePoint) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
        this.newMaxPointsReason = newMaxPointsReason;
        this.timePoint = timePoint;
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
    public Util.Triple<Double, Double, Boolean> internalApplyTo(RacingEventService toState) throws NoWindException {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceColumn raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                if (raceColumn == null) {
                    throw new IllegalArgumentException("Didn't find race "+getColumnName()+" in leaderboard "+getLeaderboardName());
                }
                leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, newMaxPointsReason);
                updateStoredLeaderboard(toState, leaderboard);
                Entry updatedEntry = leaderboard.getEntry(competitor, raceColumn, timePoint);
                boolean isScoreCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn, timePoint);
                return new Util.Triple<Double, Double, Boolean>(updatedEntry.getTotalPoints(), updatedEntry.getNetPoints(), isScoreCorrected);
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
    }

}
