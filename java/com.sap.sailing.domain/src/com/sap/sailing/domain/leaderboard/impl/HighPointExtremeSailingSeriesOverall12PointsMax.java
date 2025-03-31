package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;

/**
 * A variant of the {@link HighPoint} scoring scheme which breaks ties differently and which assigns a score of 10 to
 * the winner of a regatta, and one less for each subsequent position. This scheme is used particularly by the Extreme
 * Sailing Series' overall leaderboard and can only be applied to {@link MetaLeaderboard}s.
 * <p>
 * 
 * From the Notices of Race: "13.5: If there is a tie in the Series score between two or more boats at any time, the tie
 * shall be broken in favour of the boat that has won the most Regattas. If a tie still remains, it shall be broken in
 * favour of the boat that had the better place at the last Regatta sailed."
 * 
 * @author Axel Uhl (d043530)
 * @author Simon Marcel Pamies
 * 
 */
public class HighPointExtremeSailingSeriesOverall12PointsMax extends AbstractHighPointExtremeSailingSeriesOverall {
    private static final long serialVersionUID = -2500858156511889174L;

    private static final int MAX_POINTS = 12;
    
    public HighPointExtremeSailingSeriesOverall12PointsMax() {
        super(MAX_POINTS);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_ESS_OVERALL_12;
    }

}
