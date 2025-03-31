import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;
import com.sap.sailing.domain.test.LeaderboardCourseChangeTest;

public class LeaderboardCourseChangeWithEliminationTest extends LeaderboardCourseChangeTest {
    @Override
    protected RegattaLeaderboardWithEliminations createRegattaLeaderboard(Regatta regatta,
            ThresholdBasedResultDiscardingRule discardingRule) {
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                ()->super.createRegattaLeaderboard(regatta, discardingRule), "Test leaderboard with elimination");
    }
}
