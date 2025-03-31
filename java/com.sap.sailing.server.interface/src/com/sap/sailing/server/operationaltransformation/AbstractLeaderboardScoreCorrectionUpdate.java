package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

/**
 * The result is a {@link Triple} with the new total points in {@link Triple#getA() a}, the new net points in
 * {@link Triple#getB() b}, and whether or not the result is obtained from the correction ("isCorrected") as
 * {@link Triple#getC() c}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractLeaderboardScoreCorrectionUpdate extends AbstractLeaderboardColumnOperation<Util.Triple<Double, Double, Boolean>> {
    private static final long serialVersionUID = -977025759476022993L;
    private final String competitorIdAsString;
    private final TimePoint timePoint;
    
    /**
     * @param timePoint the time point for which to deliver leaderboard results as the result of this operation
     */
    public AbstractLeaderboardScoreCorrectionUpdate(String leaderboardName, String columnName, String competitorIdAsString, TimePoint timePoint) {
        super(leaderboardName, columnName);
        this.competitorIdAsString = competitorIdAsString;
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
    
    protected TimePoint getTimePoint() {
        return timePoint;
    }

    protected String getCompetitorIdAsString() {
        return competitorIdAsString;
    }

    @Override
    public Util.Triple<Double, Double, Boolean> internalApplyTo(RacingEventService toState) throws NoWindException {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        Double newTotalPoints;
        Double newNetPoints;
        boolean isScoreCorrected;
        if (leaderboard != null) {
            Competitor competitor = leaderboard.getCompetitorByIdAsString(competitorIdAsString);
            if (competitor != null) {
                RaceColumn raceColumn = leaderboard.getRaceColumnByName(getColumnName());
                newTotalPoints = updateScoreCorrection(leaderboard, competitor, raceColumn);
                newNetPoints = leaderboard.getEntry(competitor, raceColumn, getTimePoint()).getNetPoints();
                isScoreCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn, getTimePoint());
            } else {
                throw new IllegalArgumentException("Didn't find competitor with ID "+competitorIdAsString+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        updateStoredLeaderboard(toState, leaderboard);
        return new Util.Triple<Double, Double, Boolean>(newTotalPoints, newNetPoints, isScoreCorrected);
    }
    
    /**
     * Perform the actual score correction update on the leaderboard and its {@link Leaderboard#getScoreCorrection() score corrections}
     * and then return the competitor's new total points.
     */
    protected abstract Double updateScoreCorrection(Leaderboard leaderboard, Competitor competitor, RaceColumn raceColumn);
}
