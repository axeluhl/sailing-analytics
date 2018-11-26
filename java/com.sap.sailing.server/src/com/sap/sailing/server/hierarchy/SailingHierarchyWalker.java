package com.sap.sailing.server.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;

/**
 * Util to walk the sailing domain object hierarchy using visitors. This util encapsulates specific hierarchy logic
 * necessary because the hierarchy is not always obvious and consistent. For event series, the {@link LeaderboardGroup}
 * is meant to be the top level domain object and defines a hierarchy containing several {@link Event Events} and
 * {@link Leaderboard Leaderboards}. For other event types, the {@link LeaderboardGroup} is only a related object of the
 * {@link Event} defining the top level point of a hierarchy.
 */
public final class SailingHierarchyWalker {

    private SailingHierarchyWalker() {
    }

    public static void walkFromEvent(final Event event, final boolean includeLeaderboardGroupsWithOverallLeaderboard,
            final EventHierarchyVisitor visitor) {
        final Map<Leaderboard, Set<LeaderboardGroup>> leaderboardsToLeaderboardGroups = new HashMap<>();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            if (includeLeaderboardGroupsWithOverallLeaderboard || !lg.hasOverallLeaderboard()) {
                visitor.visit(lg);
            }
            for (Leaderboard lb : lg.getLeaderboards()) {
                Util.add(leaderboardsToLeaderboardGroups, lb, lg);
            }
        }
        for (Map.Entry<Leaderboard, Set<LeaderboardGroup>> entry : leaderboardsToLeaderboardGroups.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }
    }

    public static void walkFromLeaderboardGroup(RacingEventService service, final LeaderboardGroup leaderboardGroup,
            final boolean includeEventsIfLeaderboardGroupHasOverallLeaderboard,
            final LeaderboardGroupHierarchyVisitor visitor) {
        boolean visitEvents = includeEventsIfLeaderboardGroupHasOverallLeaderboard
                && leaderboardGroup.hasOverallLeaderboard();

        if (visitEvents) {
            for (Event event : service.getAllEvents()) {
                if (Util.contains(event.getLeaderboardGroups(), leaderboardGroup)) {
                    visitor.visit(event);
                }
            }
        }

        for (Leaderboard lb : leaderboardGroup.getLeaderboards()) {
            visitor.visit(lb);
        }
        if (leaderboardGroup.hasOverallLeaderboard()) {
            visitor.visit(leaderboardGroup.getOverallLeaderboard());
        }
    }

    public static void walkFromLeaderboard(final Leaderboard leaderboard, final LeaderboardHierarchyVisitor visitor) {
        for (TrackedRace race : leaderboard.getTrackedRaces()) {
            visitor.visit(race);
        }
        for (Competitor comp : leaderboard.getCompetitors()) {
            visitor.visit(comp);
        }
        for (Boat boat : leaderboard.getAllBoats()) {
            visitor.visit(boat);
        }
    }
}
