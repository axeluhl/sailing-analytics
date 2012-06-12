package com.sap.sailing.server.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class LeaderboardDiscardingRulesTest {

    private static final String LEADERBOARDNAME = "TESTBOARD";

    private RacingEventService racingEventService;

    @Before
    public void setUp() {
        racingEventService = new RacingEventServiceImpl();
    }

    @Test
    public void testDiscardingRoules() {
        racingEventService.removeLeaderboard(LEADERBOARDNAME);
        racingEventService.addFlexibleLeaderboard(LEADERBOARDNAME, new int[] { 1, 4 });
        FlexibleLeaderboard leaderboard = (FlexibleLeaderboard) racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboard);
        int[] discardingRoulesNew = new int[] { 1, 5 };
        leaderboard.setResultDiscardingRule(new ResultDiscardingRuleImpl(discardingRoulesNew));
        racingEventService.updateStoredFlexibleLeaderboard(leaderboard);
        Leaderboard leaderboardNew = racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboardNew);
        int[] result = leaderboardNew.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces();
        assertArrayEquals(discardingRoulesNew, result);
    }

}
