package com.sap.sailing.domain.leaderboard.meta;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A meta leaderboard which considers all regular leaderboards of a {@link LeaderboardGroup}. To stay up to date,
 * this meta leaderboard registers itself as a {@link LeaderboardGroupListener} on the leaderboard group it
 * represents. This way, whenever a leaderboard is added to or removed from the group, this meta leaderboard
 * can in turn inform its {@link RaceColumnListener}s about the impact this change has on the set of
 * {@link TrackedRaces} and therefore the competitors attached to this meta leaderboard.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LeaderboardGroupMetaLeaderboard extends AbstractMetaLeaderboard implements LeaderboardGroupListener, RaceColumnListener {
    private static final long serialVersionUID = 8087872002175528002L;
    
    private static final String OVERALL = "Overall"; // TODO consider i18n, see also bug 923

    private final LeaderboardGroup leaderboardGroup;

    public LeaderboardGroupMetaLeaderboard(LeaderboardGroup leaderboardGroup, ScoringScheme scoringScheme,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(leaderboardGroup.getName()+" "+OVERALL, scoringScheme, resultDiscardingRule);
        this.leaderboardGroup = leaderboardGroup;
    }

    @Override
    protected Iterable<Leaderboard> getLeaderboards() {
        return leaderboardGroup.getLeaderboards();
    }

    @Override
    public void leaderboardAdded(LeaderboardGroup group, Leaderboard leaderboard) {
        leaderboard.addRaceColumnListener(this);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
                }
            }
        }
    }

    @Override
    public void leaderboardRemoved(LeaderboardGroup group, Leaderboard leaderboard) {
        leaderboard.removeRaceColumnListener(this);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
                }
            }
        }
    }
    
}
